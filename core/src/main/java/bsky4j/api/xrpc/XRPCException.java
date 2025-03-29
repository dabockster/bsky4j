package bsky4j.api.xrpc;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when an XRPC operation fails.
 * Provides detailed information about the failure for better diagnostics.
 */
public class XRPCException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final int statusCode;
    private final String method;
    private final Map<String, Object> params;
    private final List<String> errorDetails;
    
    /**
     * Creates a new XRPCException with the specified status code and error message.
     * 
     * @param statusCode The HTTP status code
     * @param errorMessage The error message from the server
     */
    public XRPCException(int statusCode, String errorMessage) {
        this(statusCode, errorMessage, null, null);
    }
    
    /**
     * Creates a new XRPCException with the specified cause.
     * 
     * @param cause The cause of the exception
     */
    public XRPCException(Throwable cause) {
        this(0, cause.getMessage(), null, null);
        initCause(cause);
    }
    
    /**
     * Creates a new XRPCException with detailed error information.
     * 
     * @param statusCode The HTTP status code
     * @param errorMessage The error message from the server
     * @param method The XRPC method that failed
     * @param params The request parameters
     */
    public XRPCException(int statusCode, String errorMessage, String method, Map<String, Object> params) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.method = method;
        this.params = params;
        this.errorDetails = parseErrorDetails(errorMessage);
    }
    
    /**
     * Gets the HTTP status code associated with this exception.
     * 
     * @return The HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * Gets the XRPC method that failed.
     * 
     * @return The XRPC method name, or null if not available
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Gets the request parameters that were sent.
     * 
     * @return The request parameters, or null if not available
     */
    public Map<String, Object> getParams() {
        return params;
    }
    
    /**
     * Gets detailed error information parsed from the error message.
     * 
     * @return A list of error details
     */
    public List<String> getErrorDetails() {
        return errorDetails;
    }
    
    /**
     * Parses detailed error information from the error message.
     * 
     * @param errorMessage The error message to parse
     * @return A list of error details
     */
    private static List<String> parseErrorDetails(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return List.of();
        }
        
        // Split on common error message delimiters
        return List.of(errorMessage.split("[\r\n|;|,|\.]"));
    }
    
    /**
     * Gets a detailed error message including status code and method.
     * 
     * @return A detailed error message
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (statusCode != 0) {
            sb.append("HTTP ").append(statusCode).append(" - ");
        }
        
        if (method != null) {
            sb.append("XRPC method: ").append(method).append(" - ");
        }
        
        sb.append(super.getMessage());
        
        if (!errorDetails.isEmpty()) {
            sb.append("\nDetails: ");
            errorDetails.forEach(detail -> sb.append("\n- ").append(detail));
        }
        
        return sb.toString();
    }
}
