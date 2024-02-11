package ddbcar;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddbcar.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class AppTest {

    private static final Logger logger = LogManager.getLogger(AppTest.class);

    @Test
    public void sanityCheck() {
        try (InputStream testStream = this.getClass().getResourceAsStream("/test-stream-all-fields.json")) {
            String testJson = IoUtils.toUtf8String(Objects.requireNonNull(testStream));
            ObjectMapper mapper = new ObjectMapper();
            DynamodbEvent dynamodbEvent = mapper.readValue(testJson, DynamodbEvent.class);
            //Will actually insert data into DynamoDB after STS is cofigured properly
            /*App app = new App();
            Assert.assertEquals(Constants.SUCCESS, app.handleRequest(dynamodbEvent, getTestContext()));*/
            Assert.assertNotNull(dynamodbEvent);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static Context getTestContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return null;
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        };
    }
}
