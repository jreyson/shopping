package com.shopping.api.tools;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.shopping.core.tools.CommUtil;

import sun.misc.BASE64Encoder;

public class Test1 {

	@Test
	public void test(){
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String date = dateFormat.format(now);
		
		
		String pri = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKhu01qdYDr/yafI2TUkhBUcJK+2QEbeXIp1gXRwCLNP3jdDpR9hw+Ngm5NaOvJULubBfb8irCs7JZEkn5sLtElmGJazIddO0uKnHBqXddV3QpXfZgo4mtFpG6kanV9h6w9+DHOxGYznHAl5RuLIXBYCey2vlNnl3M6UOlNBsn6xAgMBAAECgYBrdhHozWG5IrsxDmbujfarVUJezQOjc3lNaX0HofcbGEjpr4HpTMHjDx8TW00ikO0/kpG84c1A48KrINen30bNSF+2H8bkfX+ALqJ8e2cgwl5saseKbSNjPO9mpAu4dIkDlpYR3EMU0yI4lFj1qBLlqAhWfrWyBWrsG/oi0vPThQJBANiI0w53CN8MhzqzgNDmoku153EbFqUvfHGbJV+XlGXJGdoYhkX+eFfCP/6nW6CQiTeClWNbzUuE7/GqID5U4hsCQQDHIad4eyWxK1ZiQnA9xEyf2fRo5JtF5bfkY8jHAIyKO6Lz0qrSastxm3ux/3L4KKA2vtUAhFn6dF3eBHE2CA8jAkEArKaUGoGA+lAD9yMvP+HVYCa/Tmj56mXthKve5dR3x5zMVyCc12xqShchbYvFvEXikvc05A9Lpr5tjzRGF00ZJwJAbGQUPY+Ct8poLfoOEID+WHCSClqNbmGZVFdAXZod5cyKaX+9feWlscQ5c20hzpSGiOYdGTfxplObGJOAcDG40wJBALbTtktQJh4gvzsK68kD3CJWu2HeLd96L2yeJu1cfFCND81WX2ayshzDM3B/viF+miQoneNCpqn781FP0W++BRM=";
		String pri2 = "MIICXQIBAAKBgQCobtNanWA6/8mnyNk1JIQVHCSvtkBG3lyKdYF0cAizT943Q6UfYcPjYJuTWjryVC7mwX2/IqwrOyWRJJ+bC7RJZhiWsyHXTtLipxwal3XVd0KV32YKOJrRaRupGp1fYesPfgxzsRmM5xwJeUbiyFwWAnstr5TZ5dzOlDpTQbJ+sQIDAQABAoGAa3YR6M1huSK7MQ5m7o32q1VCXs0Do3N5TWl9B6H3GxhI6a+B6UzB4w8fE1tNIpDtP5KRvOHNQOPCqyDXp99GzUhfth/G5H1/gC6ifHtnIMJebGrHim0jYzzvZqQLuHSJA5aWEdxDFNMiOJRY9agS5agIVn61sgVq7Bv6ItLz04UCQQDYiNMOdwjfDIc6s4DQ5qJLtedxGxalL3xxmyVfl5RlyRnaGIZF/nhXwj/+p1ugkIk3gpVjW81LhO/xqiA+VOIbAkEAxyGneHslsStWYkJwPcRMn9n0aOSbReW35GPIxwCMijui89Kq0mrLcZt7sf9y+CigNr7VAIRZ+nRd3gRxNggPIwJBAKymlBqBgPpQA/cjLz/h1WAmv05o+epl7YSr3uXUd8eczFcgnNdsakoXIW2LxbxF4pL3NOQPS6a+bY80RhdNGScCQGxkFD2PgrfKaC36DhCA/lhwkgpajW5hmVRXQF2aHeXMiml/vX3lpbHEOXNtIc6UhojmHRk38aZTmxiTgHAxuNMCQQC207ZLUCYeIL87CuvJA9wiVrth3i3fei9snibtXHxQjQ/NVl9msrIcwzNwf74hfpokKJ3jQqap+/NRT9FvvgUT";
		
		JSONObject biz_content = new JSONObject();
		biz_content.put("timeout_express", "30m");
		biz_content.put("seller_id", "");
		biz_content.put("product_code", "QUICK_MSECURITY_PAY");
		biz_content.put("total_amount", "0.1");
		biz_content.put("subject", "测试一下哈");
		biz_content.put("body", "测试一下哈");
		biz_content.put("out_trade_no", "111222333");
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("app_id", "2016092001930551");
		params.put("biz_content", biz_content.toJSONString());
		params.put("charset", "utf-8");
		params.put("format", "json");
		params.put("method", "alipay.trade.app.pay");
		params.put("notify_url", "http://120.26.112.77:8080/alipay_notify.htm");
		params.put("sign_type", "RSA");
		params.put("timestamp", date);
		params.put("version", "1.0");
		
//		String content = AlipaySignature.getSignCheckContentV2(params);
//		System.out.println(content);
		try {
			String sign = AlipaySignature.rsaSign(params, pri, "utf-8");
			params.put("sign", sign);
			String content = getSignContent(params);
			System.out.println(content);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}
    public static String getSignContent(Map<String, String> sortedParams) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<String>(sortedParams.keySet());
        Collections.sort(keys);
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = sortedParams.get(key);
            if (StringUtils.areNotEmpty(key, value)) {
            	String encode_value = URLEncoder.encode(value);
                content.append((index == 0 ? "" : "&") + key + "=" + encode_value);
                index++;
            }
        }
        return content.toString();
    }
}
