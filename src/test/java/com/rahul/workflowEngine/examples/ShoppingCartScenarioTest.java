package com.rahul.workflowEngine.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rahul.workflowEngine.engine.Workflow;
import com.rahul.workflowEngine.engine.WorkflowBuilder;
import com.rahul.workflowEngine.engine.WorkflowContext;
import com.rahul.workflowEngine.engine.WorkflowEngineImpl;
import com.rahul.workflowEngine.task.FailureHandler;
import com.rahul.workflowEngine.task.Task;

public class ShoppingCartScenarioTest {

	private enum Location {
		CA, OUT_CA
	}

	private static int VALID_USER_ID = 1;
	private static int INVALID_USER_ID = 2;

	private CountDownLatch latch;

	private Task validateUser;
	private Task getItemsInUserCart;
	private Task getSellersForShoppingCartItems;
	private Task filterSellersOnlyInCalifornia;
	private FailureHandler failureHandler;

	class ShoppingCartContext implements WorkflowContext {

		private User user;
		private ShoppingCart cart;
		private Map<Item, List<Seller>> sellers = new HashMap<>();

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public Map<Item, List<Seller>> getSellers() {
			return sellers;
		}

		public void setSellers(Map<Item, List<Seller>> sellers) {
			this.sellers = sellers;
		}

		public ShoppingCart getCart() {
			return cart;
		}

		public void setCart(ShoppingCart cart) {
			this.cart = cart;
		}

	}

	@BeforeEach
	public void setup() {
		this.latch = new CountDownLatch(1);

		this.validateUser = (token, context) -> {
			if (this.validate(((ShoppingCartContext) context).getUser())) {
				System.out.println("USER VALIDATED SUCCESSFULLY");
				token.success();
			} else {
				token.failure(new RuntimeException("Invalid User"));
			}
		};

		this.getItemsInUserCart = (token, context) -> {
			ShoppingCartContext shoppingCartContext = (ShoppingCartContext) context;
			ShoppingCart shoppingCart = this.getUserShoppingCart(shoppingCartContext.getUser());
			shoppingCartContext.setCart(shoppingCart);
			System.out.println("RETRIEVED SHOPPING CART SUCCESSFULLY");
			token.success();

		};

		this.getSellersForShoppingCartItems = (token, context) -> {
			ShoppingCartContext shoppingCartContext = (ShoppingCartContext) context;

			shoppingCartContext.getCart().getItems().forEach(item -> {
				shoppingCartContext.getSellers().put(item, this.getSellers(item));
			});
			System.out.println("GOT SELLERS FOR ITEMS IN SHOPPING CART");
			token.success();
		};

		this.filterSellersOnlyInCalifornia = (token, context) -> {
			ShoppingCartContext shoppingCartContext = (ShoppingCartContext) context;

			final Map<Item, List<Seller>> sellersInCalifornia = new HashMap<>();
			shoppingCartContext.getSellers().forEach((item, sellers) -> {
				sellersInCalifornia.put(item, sellers.stream().filter(seller -> seller.getLocation() == Location.CA)
						.collect(Collectors.toList()));

			});

			shoppingCartContext.setSellers(sellersInCalifornia);
			System.out.println("FILTERING SELLERS IN CALIFORNIA");
			token.success();

		};

		this.failureHandler = (token, context, error) -> {
			System.out.println("????FAILURE IN WORKFLOW???");
			System.out.println(error.getMessage());
			((ShoppingCartContext) context).setSellers(Collections.emptyMap());
			token.success();
		};
	}

	private interface Callback<T> {

		public void success(T data);

		public void failure(Throwable error);

	}

	@FunctionalInterface
	private interface AsyncTask {
		public Object execute();
	}

	@SuppressWarnings("unchecked")
	public void performTask(AsyncTask task, Callback callback) {
		callback.success(task.execute());
	}

	@Test
	public void callback_hell() {
		User user = new User(VALID_USER_ID);
		performTask(new AsyncTask() {
			@Override
			public Object execute() {
				return validate(user);
			}
		}, new Callback() {
			@Override
			public void success(Object data) {
				System.out.println("USER VALIDATED SUCCESSFULLY");
				ShoppingCartScenarioTest.this.performTask(new AsyncTask() {
					@Override
					public Object execute() {
						return getUserShoppingCart(user);
					}
				}, new Callback() {
					@Override
					public void success(Object data) {
						System.out.println("RETRIEVED SHOPPING CART SUCCESSFULLY");
						performTask(new AsyncTask() {

							@Override
							public Object execute() {
								Map<Item, List<Seller>> sellers = new HashMap<>();
								ShoppingCart cart = (ShoppingCart) data;
								cart.getItems().forEach(item -> {
									sellers.put(item, getSellers(item));
								});
								return sellers;
							}
						}, new Callback() {
							@Override
							public void success(Object data) {
								System.out.println("GOT SELLERS FOR ITEMS IN SHOPPING CART");
								displaySellers((Map<Item, List<Seller>>) data);
							}

							// ???WHERE TO HANDLE FAILURE????
							@Override
							public void failure(Throwable error) {
							}
						});
					}

					@Override
					public void failure(Throwable error) {
					}
				});
			}

			@Override
			public void failure(Throwable error) {

			}
		});
		// ???WHERE TO HANDLE FAILURE????

	}

	@Test
	public void findSellers_ForItems_InShoppingCart() throws InterruptedException {

		// CREATING A SHOPPING CART CONTEXT - passed on to the tasks
		ShoppingCartContext shoppingCartContext = new ShoppingCartContext();
		// setting the user in the context
		shoppingCartContext.setUser(new User(VALID_USER_ID));
//		shoppingCartContext.setUser(new User(INVALID_USER_ID));

		// INSTANTIATION OF WORKFLOW USING A WORKFLOW BUILDER
		// 1. ADDING ALL THE TASKS
		// 2. FAILURE HANDLER
		// 3. CONTEXT
		Workflow workflow = new WorkflowBuilder()
				.addTasks(List.of(validateUser, getItemsInUserCart, getSellersForShoppingCartItems,
						filterSellersOnlyInCalifornia))
				.addFailureHandler(failureHandler).withContext(shoppingCartContext).build();

		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			System.out.println("***Workflow executed successfully***");
			displaySellers(shoppingCartContext.getSellers());
			latch.countDown();
		});

		// Waiting for the promise to be successful
		latch.await();
	}

	private void displaySellers(Map<Item, List<Seller>> itemSellers) {
		itemSellers.forEach((item, sellers) -> {
			System.out.println("==================================================================================");
			System.out.println("ITEM - " + item.getName());
			System.out.println("SELLERS - ");

			sellers.forEach(seller -> System.out.println("Id - " + seller.id + " Location - " + seller.location));
			System.out.println("==================================================================================");
		});
	}

	/******************************************************************************
	 ********************************* MOCKING API*********************************
	 *****************************************************************************/
	private boolean validate(User user) {

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean result = user.getUserId() == VALID_USER_ID ? true : false;
		return result;

	}

	private ShoppingCart getUserShoppingCart(User user) {

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ShoppingCart shoppingCart = new ShoppingCart();
		shoppingCart.setItems(List.of(new Item(1, "Item 1"), new Item(2, "Item 2"), new Item(3, "Item 3")));
		return shoppingCart;
	}

	private List<Seller> getSellers(final Item item) {
		try {
			TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return IntStream.range(1, getRandomInteger(6, 3)).mapToObj(i -> new Seller("SELLER_" + item.name + "_" + i,
				getRandomInteger(10, 1) > 4 ? Location.OUT_CA : Location.CA)).collect(Collectors.toList());

	}

	public static int getRandomInteger(int maximum, int minimum) {
		return ((int) (Math.random() * (maximum - minimum))) + minimum;
	}

	class User {

		private int userId;

		public User(int userId) {
			this.userId = userId;
		}

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

	}

	class ShoppingCart {

		private List<Item> items;

		public ShoppingCart() {
			this.items = new ArrayList<ShoppingCartScenarioTest.Item>();
		}

		public List<Item> getItems() {
			return items;
		}

		public void setItems(List<Item> items) {
			this.items = items;
		}

	}

	class Item {

		int id;
		String name;

		public String getName() {
			return name;
		}

		public Item(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

	}

	class Seller {

		String id;

		Location location;

		public Seller(String id, Location location) {
			this.id = id;
			this.location = location;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}

	}
}
