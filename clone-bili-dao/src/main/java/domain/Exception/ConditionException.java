package domain.Exception;

public class ConditionException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String code; //jsonresponse 响应状态码

    public ConditionException(String code, String name) {
        super(name);
        this.code = code;
    }

    public ConditionException(String name) {
        super(name);
        code = "500"; //通用错误返回状态码
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
