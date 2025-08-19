package sg.edu.ntu.garang_guni_backend.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The UserRole enum represents different roles a user can have in the system.
 * This enum has three
 * constants: USER, BUYER, and ADMIN, each mapped to their corresponding string
 * representations.
 * The enum provides custom JSON handling by annotating methods for
 * serialization and deserialization.
 */
public enum UserRole {
    /**
     * Represents the "User" role in the system.
     */
    USER("USER"),

    /**
     * Represents the "Buyer" role in the system.
     */
    BUYER("BUYER"),

    /**
     * Represents the "Admin" role in the system.
     */
    ADMIN("ADMIN");

    private final String roleName;

    /**
     * Private constructor for the UserRole enum.
     *
     * @param roleName the string representation of the UserRole.
     */
    private UserRole(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Returns the string representation of the UserRole for JSON serialization.
     *
     * @return the UserRole as a string (e.g., "User", "Buyer", "Admin")
     */
    @JsonValue
    public String getRoleName() {
        return roleName;
    }

    /**
     * Parses a string value into the corresponding {@code UserRole} enum constant.
     * This method is
     * case-insensitive and is used for custom deserialization.
     * For example, a string value of "Buyer" or "buyer" would return {@code BUYER}.
     *
     * @param roleName the string representation of the UserRole (e.g., "User",
     *                 "Buyer","Admin")
     * @return the corresponding {@code UserRole} enum constant
     * @throws IllegalArgumentException if the provided roleName value does not
     *                                  match any enum
     *                                  constant
     */
    @JsonCreator
    public static UserRole parseUserRole(String roleName) {
        for (UserRole role : UserRole.values()) {
            if (role.getRoleName().equalsIgnoreCase(roleName)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid user role value: " + roleName);
    }
}
