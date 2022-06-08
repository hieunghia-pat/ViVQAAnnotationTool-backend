package uit.spring.annotation.security;

public enum UserPermission {
    IMAGE_READ("image:read"),
    IMAGE_WRITE("image:write"),
    ANNOTATION_READ("annotation:read"),
    ANNOTATION_WRITE("annotation:write"),
    ANNOTATOR_READ("annotator:read"),
    ANNOTATOR_WRITE("annotator:write"),
    ADMIN_READ("admin:read"),
    ADMIN_WRITE("admin:write"),
    SUBSET_READ("subset:read"),
    SUBSET_WRITE("subset:write"),
    ASSIGNMENT_READ("assignment:read"),
    ASSIGNMENT_WRITE("assignment:write");

    private final String permission;

    private UserPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

}
