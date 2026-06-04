package com.cts.mfrp.oneappetite.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final String LOGIN_HEADING = "Welcome back to One Appetite";
    public static final String LOGIN_TAGLINE = "Fueling your workday, one bite at a time";
    public static final String CREATE_ACCOUNT_HEADING = "Create Account";
    public static final String LOCATION_HEADING = "Find your Flavor.";
    public static final String MY_ORDERS_HEADING = "My Orders";
    public static final String CART_HEADING = "Your Cart";
    public static final String ORDER_CONFIRMED_HEADING = "Order Confirmed!";
    public static final String ADMIN_HEADING = "Manage Users.";

    public static final String EMPLOYEE_ROLE = "Employee";
    public static final String VENDOR_ROLE = "Vendor";
    public static final String ADMIN_ROLE = "Admin";

    public static final String ROUTE_LOGIN = "/login";
    public static final String ROUTE_SIGNUP = "/signup";
    public static final String ROUTE_DASHBOARD = "/dashboard";
    public static final String ROUTE_VENDOR_DASHBOARD = "/vendor/dashboard";
    public static final String ROUTE_ADMIN_DASHBOARD = "/admin/dashboard";
    public static final String ROUTE_CART = "/cart";
    public static final String ROUTE_MY_ORDERS = "/my-orders";
    public static final String ROUTE_SETTINGS = "/settings";
    public static final String ROUTE_ADMIN_SETTINGS = "/admin/settings";
    public static final String ROUTE_VENDOR_SETTINGS = "/vendor/settings";
    public static final String ROUTE_VENDOR_MENU_MGMT = "/vendor/menu";

    public static final String TOAST_OTP_SENT = "OTP sent to your registered number";
    public static final String TOAST_THEME_DARK = "Theme updated to Dark Mode";
    public static final String TOAST_THEME_LIGHT = "Theme updated to Light Mode";
    public static final String TOAST_PASSWORD_RESET = "Password reset successfully";
    public static final String TOAST_ACCOUNT_CREATED = "Account created";
    public static final String ERR_EMAIL_REQUIRED_REGEX = "(?i)email.*(required|enter)";
    public static final String ERR_PASSWORD_LENGTH = "Must be at least 8 characters.";
    public static final String ERR_ROLE_REQUIRED = "Please select a role to continue.";
    public static final String ERR_OTP_INVALID = "Invalid or expired OTP. Please request a new one.";
    public static final String ERR_INSUFFICIENT_WALLET = "Insufficient wallet balance. Please top up and try again.";
    public static final String ERR_PHONE_INVALID = "Invalid format. Must be 10 digits";
    public static final String ERR_ADMIN_SELF_DEACTIVATE = "You can't deactivate your own admin account.";
    public static final String ERR_NO_USERS = "No users match the current filters.";
    public static final String ERR_NO_VENDORS = "No vendors match your filters.";
    public static final String ERR_NO_ORDERS = "No orders to show here yet.";
}
