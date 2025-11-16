package hypergraphdb.models;

public class User {
    private String name;
    private String email;
    private String password;
    private String role;

    public User() {}
    public User(String name, String email, String password) {
        this(name, email, password, "user"); // mặc định là user
    }
    public User(String name, String email, String password, String role ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role= role;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
