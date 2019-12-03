package com.rahul.workflowEngine.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.rahul.workflowEngine.engine.Workflow;
import com.rahul.workflowEngine.engine.WorkflowBuilder;
import com.rahul.workflowEngine.engine.WorkflowContext;
import com.rahul.workflowEngine.engine.WorkflowEngineImpl;
import com.rahul.workflowEngine.task.Task;

public class ShoppingCartScenarioTest {

	private enum Location {
		CA, OUT_CA
	}

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

	@Test
	public void findSellers_ForItems_InShoppingCart() {

		Task validateUser = (token, context) -> {
			if (this.validate(((ShoppingCartContext) context).getUser())) {
				System.out.println("USER VALIDATED SUCCESSFULLY");
				token.success();
			} else {
				token.failure(new RuntimeException("Invalid User"));
			}
		};

		Task getItemsInUserCart = (token, context) -> {
			ShoppingCartContext shoppingCartContext = (ShoppingCartContext) context;
			ShoppingCart shoppingCart = this.getUserShoppingCart(shoppingCartContext.getUser());
			shoppingCartContext.setCart(shoppingCart);
			System.out.println("RETRIEVED SHOPPING CART SUCCESSFULLY");
			token.success();

		};

		Task getSellersForShoppingCartItems = (token, context) -> {
			ShoppingCartContext shoppingCartContext = (ShoppingCartContext) context;

			shoppingCartContext.getCart().getItems().forEach(item -> {
				shoppingCartContext.getSellers().put(item, this.getSellers(item));
			});
			System.out.println("GOT SELLERS FOR ITEMS IN SHOPPING CART");
			token.success();
		};

		ShoppingCartContext shoppingCartContext = new ShoppingCartContext();

		shoppingCartContext.setUser(new User(1));

		Workflow workflow = new WorkflowBuilder()
				.addTasks(List.of(validateUser, getItemsInUserCart, getSellersForShoppingCartItems))
				.withContext(shoppingCartContext).build();

		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			shoppingCartContext.getSellers().forEach((item, sellers) -> {
				System.out.println("ITEM - " + item.getName());
				System.out.println("SELLERS - ");

				sellers.forEach(seller -> System.out.println("Id - " + seller.id + " Location - " + seller.location));

			});

			System.out.println("***Workflow executed successfully***");
		});

	}

	private boolean validate(User user) {

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;

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
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return IntStream.range(1, getRandomInteger(5, 2)).mapToObj(i -> new Seller("SELLER_" + item.name + "_" + i,
				getRandomInteger(3, 1) == 2 ? Location.OUT_CA : Location.CA)).collect(Collectors.toList());

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
