package ddbcar.util;

public class Constants {

    public static final String TARGET_DDB_TABLE = "Target-DDB";

    public static final String TARGET_STS_ROLE_ARN = "arn:aws:iam::381492012287:role/DDB-CAR-Role";

    public static final String TARGET_STS_SESSION_NAME = "DDB-CAR-Session";

    public static final Integer STS_TTL_SECONDS = 900; //minimum seconds for STS

    public static final String STS_EXTERNAL_ID = "ddb-car";

    public static final String DDB_EVENT_REMOVE = "REMOVE";

    public static final String SUCCESS = "success";

    public static final String FAILURE = "failure";
}
