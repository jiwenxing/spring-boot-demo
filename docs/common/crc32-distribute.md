# 使用 CRC32 算法进行百分比切量控制
---


CRC32、MD5 和 SHA1 是目前用来校验文件信息真实性的主要手段，使用这些校验算法可以发现保存或传输的信息是否受到损坏或篡改，防止文件或信息被恶意篡改。

CRC 全称为 Cyclic Redundancy Check，又叫循环冗余校验。CRC 是目前使用中最老的一种校验算法，它是由 W. Wesley Peterson 在 1961 年发表的论文中提出，CRC 把任意长度的输入通过散列算法，最终变换成固定长度的摘要输出，其结果就是散列值，按照 HASH 算法，HASH 具有单向性，不可逆性。现在应用最多的就是 CRC32 算法，它产生一个 4 字节（32 位）的校验值，一般是以 8 位十六进制数，如 FA 12 CD 45 等。

在 WinRAR、WinZIP 等软件中，也是以 CRC32 作为文件校验算法的。一般常见的简单文件校验（Simple File Verify – SFV）都是以 CRC32 算法为基础。因此 Java CRC32 实现是放在 java.util.zip 包下面。

CRC 并不能可靠地校验数据完整性，这是因为 CRC 多项式是线性结构，可以非常容易地通过改变数据方式达到 CRC 碰撞，但 CRC 算法的优点在于简便、速度快，因此用在对用户账号进行 hash 取余实现请求切量还是很适合的

相较而言，MD5 散列长度通常是 128 位，是目前被大量广泛使用的散列算法之一，主要用于密码加密和文件校验等，MD5 比 CRC 的安全可靠性要高的多，但目前也已经找到可行的破解方法。

目前 SHA1 的应用也较为广泛，主要应用于 CA 和数字证书中，另外在目前互联网中流行的 BT 软件中，也是使用 SHA1 来进行文件校验的。SHA 系列算法的摘要长度分别为：SHA 为 20 字节（160 位）、SHA256 为 32 字节（256 位）、 SHA384 为 48 字节（384 位）、SHA512 为 64 字节（512 位），由于它产生的数据摘要的长度更长，因此更难以发生碰撞，因此也更为安全，它是未来数据摘要算法的发展方向。由于 SHA 系列算法的数据摘要长度较长，因此其运算速度与 MD5 相比，也相对较慢。

切量逻辑代码实现

```java
public static boolean percentForPin(String pin, int persent) {
	if (StringUtils.isBlank(pin)) {
		return false;
	}
    CRC32 crc32 = new CRC32();
    try {
    	crc32.update(pin.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
    	return false;
    }
    int hashNum = Long.valueOf(crc32.getValue() % 100).intValue();
    return hashNum <persent;
}
```