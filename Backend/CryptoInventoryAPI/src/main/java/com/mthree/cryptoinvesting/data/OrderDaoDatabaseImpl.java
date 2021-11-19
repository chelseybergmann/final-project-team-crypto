package com.mthree.cryptoinvesting.data;

import com.mthree.cryptoinvesting.model.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Chelsey
 * @version 11/19/2021
 */

@Repository
public class OrderDaoDatabaseImpl implements OrderDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public Orders addOrder(Orders order) {
        final String INSERT_ORDER = "INSERT INTO orders(portfolioId, cName, price, amount, date) VALUES (?,?,?,?,?)" ;

        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        order.setDatePurchased(timestamp);

        jdbcTemplate.update(INSERT_ORDER, order.getPortfolioId(), order.getCryptoName(),
                order.getPrice(), order.getAmount(), order.getDatePurchased());

        int orderId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        order.setOrderId(orderId);

        return order;
    }

    @Override
    public Orders getOrderById(int orderId) {
        final String SELECT_ORDER_BY_ID = "SELECT * FROM orders WHERE orderId = ?";
        try {
            return jdbcTemplate.queryForObject(SELECT_ORDER_BY_ID, new OrdersMapper(), orderId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public List<Orders> getAllOrdersByPortfolioId(int portfolioId) {
        final String SELECT_ALL_ORDERS_BY_PORTFOLIO_ID = "SELECT * FROM orders WHERE portfolioId = ?";
        return jdbcTemplate.query(SELECT_ALL_ORDERS_BY_PORTFOLIO_ID, new OrdersMapper(),portfolioId);
    }

    @Override
    public List<Orders> getAllOrders() {
        final String SELECT_ALL_ORDERS = "SELECT * FROM orders";
        return jdbcTemplate.query(SELECT_ALL_ORDERS, new OrdersMapper());
    }
    public static final class OrdersMapper implements RowMapper<Orders> {

        @Override
        public Orders mapRow(ResultSet rs, int index) throws SQLException {

            Orders order = new Orders();
            order.setOrderId(rs.getInt("orderId"));
            order.setPortfolioId(rs.getInt("portfolioId"));
            order.setCryptoName(rs.getString("cName"));
            order.setPrice(rs.getFloat("price"));
            order.setAmount(rs.getFloat("amount"));

           //get the timestamp for the datePurchased variable
            Timestamp timestamp = rs.getTimestamp("date");
            order.setDatePurchased(timestamp.toLocalDateTime().truncatedTo(ChronoUnit.SECONDS));

            return order;
        }
    }
}

