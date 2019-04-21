多级联动下拉框的 jQuery 实现
---

经常会遇到多级联动下拉框的场景，比如商品的三级分类，比如地址选择等等。

这里以商品三级分类为例，页面初始加载所有的一级分类，选中某个一级分类时，自动加载对应的二级分类列表，二三级分类一次类推，重新修改一级分类需要重置二级分类的同时清空三级分类，jQuery实现如下

### html

```html
<div class="form-group" id="categoryRow">
	<label for="categoryId" class="col-sm-1 control-label"><@spring.message "index.search.cat"/></label>
	<div class="col-sm-1">
		<select class="form-control category1" name="categoryOne">
			<option value=""><@spring.message "index.search.cat.one"/></option>
		</select>
	</div>
	<div class="col-sm-1">
		<select class="form-control category2" name="categoryTwo">
			<option value=""><@spring.message "index.search.cat.two"/></option>
		</select>
	</div>
	<div class="col-sm-1">
		<select class="form-control category3" name="categoryThree">
			<option value=""><@spring.message "index.search.cat.three"/></option>
		</select>
	</div>
</div>
```

### js

```javascript
$(function() {
	var cateJson;
	
	// 获取json数据
	function loadData(url, level){
		$.getJSON(url, function(result) {
			if(result.success){
				cateJson = result.data;
				console.log("cateJson = " + cateJson)
				initCategory(level);
			}
		});
	}
	
	//  初始化分类列表
	function initCategory(level){
		var temp_html;
		$.each(cateJson, function(i, item) {
			temp_html += "<option value='" + item.productSortId + "'>"
					+ item.name + "</option>";
		});
		$(".category" + level +  " option:not(:first)").remove(); //先清空选项
		$("#categoryRow").find(".category"+level).append(temp_html);
	};
	
	// 一级分类变化
	$("#categoryRow").find(".category1").change(function() {
		const fid = $(this).val();
		loadData("/common/getCategoryList?categoryLevel=1&fid="+fid, 2);
		$(".category3 option:not(:first)").remove(); //一级变化需要清空三级历史
	});
	
	// 二级分类变化
	$("#categoryRow").find(".category2").change(function() {
		const fid = $(this).val();
		loadData("/common/getCategoryList?categoryLevel=2&fid="+fid, 3);
	});
	
	loadData("/common/getCategoryList?categoryLevel=0", 1);
	
});
```