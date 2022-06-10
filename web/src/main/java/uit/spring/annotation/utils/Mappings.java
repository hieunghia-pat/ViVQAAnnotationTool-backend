package uit.spring.annotation.utils;

public final class Mappings {

    public static final String API = "/api/v1";
    public static final String GET = "/get";
    public static final String ADD = "/add";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete";
    public static final String SUBSET = "/subset";
    public static final String SUBSETS = "/subsets";
    public static final String ASSIGNMENT = "/assignment";
    public static final String POS = "/pos";
    public static final String STATISTICS = "/statistics";
    public static final String IMAGE = "/image";
    public static final String IMAGES = "/images";
    public static final String ANNOTATION = "/annotation";
    public static final String ANNOTATION_PER_IMAGE = "/annotation-per-image";
    public static final String ANNOTATIONS = "/annotations";

    public static final String ADMIN_API = "/api/v1/admins";
    public static final String ADMIN_GET_API = ADMIN_API + GET;
    public static final String ADMIN_ADD_API = ADMIN_API + ADD;
    public static final String ADMIN_UPDATE_API = ADMIN_API + UPDATE;
    public static final String ADMIN_DELETE_API = ADMIN_API + DELETE;
    public static final String ADMIN_GET_SUBSET_API = ADMIN_API + GET + SUBSETS;
    public static final String ADMIN_UPDATE_SUBSET_API = ADMIN_API + UPDATE + SUBSETS;
    public static final String ANNOTATOR_API = "/api/v1/annotators";
    public static final String ANNOTATOR_GET_API = ANNOTATOR_API + GET;
    public static final String ANNOTATOR_ADD_API = ANNOTATOR_API + ADD;
    public static final String ANNOTATOR_UPDATE_API = ANNOTATOR_API + UPDATE;
    public static final String ANNOTATOR_DELETE_API = ANNOTATOR_API + DELETE;
    public static final String ANNOTATOR_GET_IMAGES_API = ANNOTATOR_API + IMAGES + GET;

    public static final String LOGIN = "/login";
    public static final String API_LOGIN = API + LOGIN;
    public static final String REFRESH_TOKEN = "/refresh_token";
    public static final String API_REFRESH_TOKEN = API + REFRESH_TOKEN;

    public static final String SUBSETS_API = "/api/v1/subsets";
    public static final String SUBSETS_GET_API = SUBSETS_API + GET;

    public static final String IMAGES_API = API + IMAGES;
    public static final String ANNOTATIONS_API = API + ANNOTATIONS;
    public static final String ASSIGNMENT_API = API + ASSIGNMENT;

    private Mappings() {

    }
}
