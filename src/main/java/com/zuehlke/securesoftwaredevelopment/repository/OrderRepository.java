package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    private DataSource dataSource;

    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public List<Food> getMenu(int id) {
        List<Food> menu = new ArrayList<>();
        String sqlQuery = "SELECT id, name FROM food WHERE restaurantId=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {
            while (rs.next()) {
                menu.add(createFood(rs));
            }

        }  catch (SQLException e) {
            LoggerFactory.getLogger(OrderRepository.class).warn(String.format("An error occurred while selecting fetching the menu! Message: %s", e.getMessage()));
        }

        return menu;
    }

    private Food createFood(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Food(id, name);
    }

    public void insertNewOrder(NewOrder newOrder, int userId) {
        LocalDate date = LocalDate.now();
        String sqlQuery = "INSERT INTO delivery (isDone, userId, restaurantId, addressId, date, comment) VALUES (FALSE, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, userId);
            statement.setInt(2, newOrder.getRestaurantId());
            statement.setInt(3, newOrder.getAddress());
            statement.setString(4, date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth());
            statement.setString(5, newOrder.getComment());
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggerFactory.getLogger(OrderRepository.class).warn(String.format("An error occurred while trying to create new order! Message: %s", e.getMessage()));
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            sqlQuery = "SELECT MAX(id) FROM delivery";
            ResultSet rs = statement.executeQuery(sqlQuery);

            if (rs.next()) {

                int deliveryId = rs.getInt(1);
                sqlQuery = "INSERT INTO delivery_item (amount, foodId, deliveryId)" +
                        "values";
                for (int i = 0; i < newOrder.getItems().length; i++) {
                    FoodItem item = newOrder.getItems()[i];
                    String deliveryItem = "";
                    if (i > 0) {
                        deliveryItem = ",";
                    }
                    deliveryItem += "(" + item.getAmount() + ", " + item.getFoodId() + ", " + deliveryId + ")";
                    sqlQuery += deliveryItem;
                }
                System.out.println(sqlQuery);
                statement.executeUpdate(sqlQuery);
            }

        } catch (SQLException e) {
            LoggerFactory.getLogger(OrderRepository.class).warn(String.format("An error occurred while trying to insert order items! Message: %s", e.getMessage()));
        }


    }

    public Object getAddresses(int userId) {
        List<Address> addresses = new ArrayList<>();
        String sqlQuery = "SELECT id, name FROM address WHERE userId=" + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {
            while (rs.next()) {
                addresses.add(createAddress(rs));
            }

        } catch (SQLException e) {
            LoggerFactory.getLogger(OrderRepository.class).warn(String.format("An error occurred while selecting addresses for user! Message: %s", e.getMessage()));
        }
        return addresses;
    }

    private Address createAddress(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Address(id, name);

    }
}
