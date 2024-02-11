package ddbcar;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.Record;
import ddbcar.model.TargetDDBSchema;
import ddbcar.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.StsException;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<DynamodbEvent, Serializable> {

    private static final Logger logger = LogManager.getLogger(App.class);

    @Override
    public String handleRequest(final DynamodbEvent input, final Context context) {
        try {
            List<DynamodbEvent.DynamodbStreamRecord> records = input.getRecords();

            for (Record record : records) {
                logger.info("Received record with eventId: {}", record.getEventID());

                if (Objects.nonNull(record.getDynamodb().getKeys()) && Objects.nonNull(record.getDynamodb().getNewImage())) {
                    logger.info("Received record with keys: {}", record.getDynamodb().getKeys());
                    logger.info("Received record with newImage: {}", record.getDynamodb().getNewImage());
                    logger.info("Received record with eventName: {}", record.getEventName());
                    logger.info("Received record with sizeBytes: {}", record.getDynamodb().getSizeBytes());

                    try (StsClient stsClient = StsClient.builder().region(Region.AP_SOUTH_1).build()) {

                        AwsSessionCredentials sessionCredentials = getAwsSessionCredentials(stsClient);

                        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.AP_SOUTH_1).credentialsProvider(AwsCredentialsProviderChain.builder().credentialsProviders(StaticCredentialsProvider.create(sessionCredentials)).build()).build();
                        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
                        DynamoDbTable<TargetDDBSchema> targetDDBTable = dynamoDbEnhancedClient.table(Constants.TARGET_DDB_TABLE, TableSchema.fromClass(TargetDDBSchema.class));
                        TargetDDBSchema targetDDBSchema = getTargetDDBSchema(record);
                        if (Constants.DDB_EVENT_REMOVE.equals(record.getEventName())) {
                            targetDDBTable.deleteItem(targetDDBTable.keyFrom(targetDDBSchema));
                        } else {
                            targetDDBTable.putItem(targetDDBSchema);
                        }
                        logger.info("{} item in table {} with Id: {} successfully", record.getEventName(), Constants.TARGET_DDB_TABLE, record.getDynamodb().getNewImage().get("Id").getN());
                    }


                }
            }
            return Constants.SUCCESS;
        } catch (StsException e) {
            logger.error("Error occurred while assuming role: {}", e.getMessage());
            return Constants.FAILURE;
        } catch (Exception e) {
            logger.error("Error occurred while processing event: {}", e.getMessage());
            return Constants.FAILURE;
        }
    }

    private static TargetDDBSchema getTargetDDBSchema(Record record) {
        TargetDDBSchema targetDDBSchema = new TargetDDBSchema();
        if (Objects.nonNull(record.getDynamodb().getNewImage().get("Id")))
            targetDDBSchema.setId(Long.valueOf(record.getDynamodb().getNewImage().get("Id").getN()));
        if (Objects.nonNull(record.getDynamodb().getNewImage().get("UpdatedDate")))
            targetDDBSchema.setUpdatedDate(record.getDynamodb().getNewImage().get("UpdatedDate").getS());
        if (Objects.nonNull(record.getDynamodb().getNewImage().get("Message")))
            targetDDBSchema.setMessage(record.getDynamodb().getNewImage().get("Message").getS());
        if (Objects.nonNull(record.getDynamodb().getNewImage().get("CheckedItem")))
            targetDDBSchema.setCheckedItem(record.getDynamodb().getNewImage().get("CheckedItem").getBOOL());
        return targetDDBSchema;
    }

    private static AwsSessionCredentials getAwsSessionCredentials(StsClient stsClient) {
        AssumeRoleRequest roleRequest = AssumeRoleRequest.builder().roleArn(Constants.TARGET_STS_ROLE_ARN).roleSessionName(Constants.TARGET_STS_SESSION_NAME).durationSeconds(Constants.STS_TTL_SECONDS).externalId(Constants.STS_EXTERNAL_ID).build();

        AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
        Credentials tempCreds = roleResponse.credentials();
        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(tempCreds.accessKeyId(), tempCreds.secretAccessKey(), tempCreds.sessionToken());

        // Display the time when the temp creds expire.
        Instant exTime = tempCreds.expiration();
        String tokenInfo = tempCreds.sessionToken();
        logger.info("The STS token {}  expires on {}", tokenInfo, exTime);
        logger.info("Assumed role successfully");
        return sessionCredentials;
    }

}
