# Junit 参数化测试
---

在写单元测试的时候经常会遇到一种情况，针对某个方法使用多组入参进行测试，这时可以每组入参写一个测试方法，但这样重复代码太多了不够优雅，而 junit 从 4.0 开始提供了一种叫做参数化测试的方式专门处理这样情况

之前的写法是这样的

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MaterialStartBoot.class)
public class GoodsSearchServiceTest {
    @Autowired
    private GoodsSearchService goodsSearchService;
    
    @Test
    public void tst() {
       SearchRequestWrap reqWrap = new SearchRequestWrap();
       reqWrap.setKeyword("数码相机");
       reqWrap.setPageNo(1);
       reqWrap.setPageSize(20);
       SearchResultWrap srw = goodsSearchService.search(JSON.toJSONString(reqWrap));
       System.out.println(JSON.toJSONString(srw));
    }

    @Test
    public void tst2() {
        SearchRequestWrap reqWrap = new SearchRequestWrap();
        reqWrap.setKeyword("手机");
        reqWrap.setPageNo(1);
        reqWrap.setPageSize(20);
        SearchResultWrap srw = goodsSearchService.search(JSON.toJSONString(reqWrap));
        System.out.println(JSON.toJSONString(srw));
    }
}
```

使用了参数化测试的代码是这样的，是不是感觉好很多。不过要注意的一点是 JUnit4 不支持多个 Runner，用了 `@RunWith(Parameterized.class)` 之后就没法再用 `@RunWith(SpringRunner.class)`，但是可以通过 `@Before` 中的 `TestContextManager` 来实现 `SpringRunner` 同样的效果

```java
@RunWith(Parameterized.class)
@SpringBootTest(classes = MaterialStartBoot.class)
public class TimlineServiceTest {
    @Autowired
    private TimlineMaterialService timlineMaterialService;
    private TestContextManager testContextManager;
    private ProductRcmdReq productRcmdReq;

    //参数数组，数组中每个元素将会被用来构造一个入参实例，每个入参实例对应一个测试用例，
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] objects = {
                {1, 10, null, SortTypeEnum.DEFAULT, "fb_1904"},
                {2, 5, "5646981", SortTypeEnum.SORT_PRICE_DESC, "fb_1904"},
                {3, 5, "5646981", SortTypeEnum.SORT_PRICE_ASC, "fb_1904"},
                {1, 10, "6985063", SortTypeEnum.SORT_COMMENTCOUNT_DESC, "fb_1904"},
                {1, 5, null, SortTypeEnum.SORT_WINSDATE_DESC, "fb_1904"},
                {1, 5, "6985063", SortTypeEnum.SORT_SALE_DESC, "fb_1904"}
        };
        return Arrays.asList(objects);
    }

    //构造函数，使用上面的参数数组初始化入参
    public TimlineServiceTest(final int page, int pageSize, String shopCate, SortTypeEnum sortTypeEnum, String pin) {
        productRcmdReq = new ProductRcmdReq();
        productRcmdReq.setSortType(sortTypeEnum.getType());
        productRcmdReq.setPage(page);
        productRcmdReq.setPageSize(pageSize);
        productRcmdReq.setShopCategory(shopCate);
        productRcmdReq.setPin(pin);
    }

    //功能相当于 @RunWith(SpringRunner.class) ，否则无法注入bean，这里同时还可以给入参初始化一些固定值
    @Before
    public void setUp() throws Exception {
        // equals to @RunWith(SpringRunner.class) in case that JUnit4 doesn’t accept multiple runners
        this.testContextManager = new TestContextManager(getClass());
        this.testContextManager.prepareTestInstance(this);
        productRcmdReq.setShopId(627277L);
    }

    //单元测试方法体
    @Test
    public void tst() throws URISyntaxException {
        final String s = timlineMaterialService.productRcmd(JSON.toJSONString(productRcmdReq));
        Type type = new TypeReference<Result<PageVo<ProductRcmdResult>>>() {
        }.getType();
        Result<PageVo<ProductRcmdResult>> result = JSONObject.parseObject(s, type);
        Assert.assertTrue(result.getStatus() == ResultEnum.SUCCESS.getStatus());
        Assert.assertTrue(result.getData().getList().size() == productRcmdReq.getPageSize());
    }
}
```



