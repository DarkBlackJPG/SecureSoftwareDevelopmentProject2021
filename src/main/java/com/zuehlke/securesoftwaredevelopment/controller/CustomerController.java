package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;

import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.config.SecurityUtil;
import com.zuehlke.securesoftwaredevelopment.domain.*;
import com.zuehlke.securesoftwaredevelopment.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.nio.file.AccessDeniedException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Controller

public class CustomerController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers-and-restaurants")
    public String customersAndRestaurants(Model model) {
        if(SecurityUtil.hasPermission("USERS_LIST_VIEW"))
            model.addAttribute("customers", customerRepository.getCustomers());
        if(SecurityUtil.hasPermission("RESTAURANT_LIST_VIEW"))
            model.addAttribute("restaurants", customerRepository.getRestaurants());
        return "customers-and-restaurants";
    }

    @GetMapping("/restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_DETAILS_VIEW')")
    public String getRestaurant(@RequestParam(name = "id", required = true) String id, Model model) {
        model.addAttribute("restaurant", customerRepository.getRestaurant(id));
        return "restaurant";
    }

    @DeleteMapping("/restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_DELETE')")
    public String deleteRestaurant(@RequestParam(name = "id", required = true) String id) {
        int identificator = Integer.valueOf(id);
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            AuditLogger
                    .getAuditLogger(CustomerController.class)
                    .audit(String.format("Restaurant.Delete == [UserID: %d; Username: %s; Restaurant ID: %d] ==",
                            user.getId(),
                            user.getUsername(),
                            identificator));
        } else {
            LOG.error("An unidentified user trying to delete restaurant");
        }
        customerRepository.deleteRestaurant(identificator);
        return "/customers-and-restaurants";
    }

    @PostMapping("/api/restaurant/update-restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_EDIT')")
    public String updateRestaurant(RestaurantUpdate restaurantUpdate, Model model) {
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            customerRepository.getRestaurants().forEach(restaurant -> {
                if (restaurant.getId() == restaurantUpdate.getId()) {
                    AuditLogger
                            .getAuditLogger(CustomerController.class)
                            .auditChange(new Entity(
                                    "Restaurant Change",
                                    SecurityUtil.getCurrentUser().getUsername(),
                                    restaurant.toString(),
                                    restaurantUpdate.toString()
                            ));
                }
            });
        } else {
            // This should never happen
            LOG.error("An unidentified user trying to update restaurant");
        }
        customerRepository.updateRestaurant(restaurantUpdate);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAuthority('USERS_DETAILS_VIEW')")
    public String getCustomer(@RequestParam(name = "id", required = true) String id, Model model, HttpSession session) {
        String csrfToken = session.getAttribute("CSRF_TOKEN").toString();
        model.addAttribute("CSRF_TOKEN", csrfToken);
        model.addAttribute("customer", customerRepository.getCustomer(id));
        model.addAttribute("addresses", customerRepository.getAddresses(id));
        return "customer";
    }

    @DeleteMapping("/customer")
    @PreAuthorize("hasAuthority('USERS_DELETE')")
    public String deleteCustomer(@RequestParam(name = "id", required = true) String id, @RequestParam(name = "CSRF_TOKEN", required = true) String csrfToken, HttpSession session) throws AccessDeniedException{
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfToken)) throw new AccessDeniedException("Forbidden");
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            AuditLogger
                    .getAuditLogger(CustomerController.class)
                    .audit(String.format("\"%d\" deleted user with id \"%s\"", SecurityUtil.getCurrentUser().getId(), id));
        } else {
            // This should never happen
            LOG.error(String.format("An unidentified user trying to delete user: %d", id));
        }
        customerRepository.deleteCustomer(id);
        return "/customers-and-restaurants";
    }

    @PostMapping("/api/customer/update-customer")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String updateCustomer(CustomerUpdate customerUpdate, Model model, HttpSession session, @RequestParam("CSRF_TOKEN") String csrfToken)
    throws AccessDeniedException
    {
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfToken)) throw new AccessDeniedException("Forbidden");
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            customerRepository.getCustomers().forEach(customer -> {
                if (customer.getId() == customerUpdate.getId()) {
                    AuditLogger
                            .getAuditLogger(CustomerController.class)
                            .auditChange(new Entity(
                                    "Restaurant Change",
                                    SecurityUtil.getCurrentUser().getUsername(),
                                    customer.toString(),
                                    customerUpdate.toString()
                            ));
                }
            });
        } else {
            // This should never happen
            LOG.error("An unidentified user trying to update user");
        }
        customerRepository.updateCustomer(customerUpdate);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }

    @DeleteMapping("/customer/address")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String deleteCustomerAddress(@RequestParam(name = "id", required = true) String id, @RequestParam(name = "CSRF_TOKEN", required = true) String csrfToken, HttpSession session) throws AccessDeniedException {
        int identificator = Integer.valueOf(id);
        String csrf = session.getAttribute("CSRF_TOKEN").toString().replace(' ', '+');
        if(!csrf.equals(csrfToken)) throw new AccessDeniedException("Forbidden");
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            AuditLogger
                    .getAuditLogger(CustomerController.class)
                    .audit(String.format("\"%d\" deleted user address with id \"%s\"", SecurityUtil.getCurrentUser().getId(), id));
        } else {
            // This should never happen
            LOG.error(String.format("An unidentified user trying to delete user address with ID: %s", id));
        }
        customerRepository.deleteCustomerAddress(identificator);
        return "/customers-and-restaurants";
    }

    @PostMapping("/api/customer/address/update-address")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String updateCustomerAddress(Address address, Model model, @RequestParam(name = "CSRF_TOKEN", required = true) String csrfToken, HttpSession session) throws AccessDeniedException{
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfToken)) throw new AccessDeniedException("Forbidden");
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            AuditLogger
                    .getAuditLogger(CustomerController.class)
                    .audit(String.format("\"%d\" updated customer  address \"%s\"", SecurityUtil.getCurrentUser().getId(), address));
        } else {
            // This should never happen
            LOG.error(String.format("An unidentified user trying to update address with ID: %d", address.getId()));
        }
        customerRepository.updateCustomerAddress(address);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }

    @PostMapping("/customer/address")
    @PreAuthorize("hasAuthority('USERS_EDIT')")
    public String putCustomerAddress(NewAddress newAddress, Model model, @RequestParam(name = "CSRF_TOKEN", required = true) String csrfToken, HttpSession session) throws AccessDeniedException{
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfToken)) throw new AccessDeniedException("Forbidden");
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            customerRepository.getAddresses(String.valueOf(newAddress.getUserId())).forEach(address -> {
                if (address.getName().equals(newAddress.getName())) {
                    AuditLogger
                            .getAuditLogger(CustomerController.class)
                            .auditChange(new Entity(
                                    "Address Change",
                                    SecurityUtil.getCurrentUser().getUsername(),
                                    address.toString(),
                                    newAddress.toString()
                            ));
                }
            });
        } else {
            // This should never happen
            LOG.error("An unidentified user trying to update address");
        }
        customerRepository.putCustomerAddress(newAddress);
        customersAndRestaurants(model);
        return "/customers-and-restaurants";
    }
}
