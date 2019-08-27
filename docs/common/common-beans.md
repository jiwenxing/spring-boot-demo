# 常用的对象定义
---

开发常用的一些通用 bean 定义，建议后期直接做一个脚手架的 repository，暂时先在这里记录一下

## result 接口返回结果通用对象

```java
public class Result<T> {
    private Integer status = 200;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Result(ResultEnum resultEnum, T data) {
        this.status = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
        this.data = data;
    }

    public Result(ResultEnum resultEnum) {
        this.status = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
    }

    //getter & setter

}
```

其中通用响应码定义如下

```java
public enum ResultEnum {
    SUCCESS(200, "成功"),
    INVALID_PARAMS(400, "参数异常"),
    AUTH_FAIL(401, "未授权"),
    SWITCH_OPEN(451, "功能已降级"),
    EXCEPTION(500, "系统异常"),
    ;

    private static final Map<Integer, ResultEnum> RESULT_ENUM_MAP = new HashMap<>();
    static {
        Arrays.stream(ResultEnum.values()).forEach(resultEnum -> RESULT_ENUM_MAP.put(resultEnum.code, resultEnum));
    }

    public static ResultEnum getResultByCode(Integer code) {
        return RESULT_ENUM_MAP.get(code);
    }

    public static String getMsgByCode(Integer code) {
        return RESULT_ENUM_MAP.containsKey(code) ? RESULT_ENUM_MAP.get(code).msg : null;
    }

    private String msg;
    private Integer code;

    private ResultEnum(Integer code, String error) {
        this.msg = error;
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public Integer getCode() {
        return this.code;
    }
}
```

## 通用的分页对象

```java
public class PageVo<T> {
	private int page;
	private int pageSize;
    private List<T> list;
    
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}


	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(final int page) {
		this.page = page;
	}
}
```