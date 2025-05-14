class UserProfile {
    String username;
    Address address;

    public UserProfile(String username) {
        this.username = username;
    }

    public String getCity() {
        return address.getCity();
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

class Address {
    String city;

    public Address(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}

public class Example1 {
    public static void main(String[] args) {
        UserProfile user = new UserProfile("JohnDoe");
        String city = user.getCity();
        System.out.println("User's city: " + city);
    }
}