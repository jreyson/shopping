package com.shopping.api.tools;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.shopping.core.tools.CommUtil;
public class ALIWapPayTools {
	//向支付宝的请求url
	public static final String RQUEST_URL="https://openapi.alipay.com/gateway.do";
	//支付宝应用的appid
	public static final String APP_ID = "2018051160132319";
	//私钥 pkcs8格式的
	public static final String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCh7gejEOiRDelOWP2MLhK6COuUJJ8gKyLA99zmLk0yHe6oObqFAVOPK3gn2mPsKQEARio9A14EMizsijaW5J9akiek5B84WjbQHDkglu0k5G3L+1oa9th76UzJQ8sqMmBkkpTV1r4EBmq3vSlM6aSJod0nBEMKm8EzvhKwCEvFJf8Hq1QXcUMTBEOI3Z+eogxoEF1A/w4qunuOZuX3nqtqvpj/bRg5sytpWB81KQJvl9SmrMIHR2ZuYwzcpNlVAeV9ecZzNTqOEWa2PK+5Z9Vjbt4rtCDcfO78GYb6ulbheKq/vMd8oojhtLdT9HoqOiQDDemJ93/duDfhBo5KVg33AgMBAAECggEAXJGviffLi8jRG/nhkDZyfClHSxwYX5JpPrkzbd8AQGs+GKtPxmM4unrWKKo/Py09pkN2OATpxSpN5jco99/gfS1aPngupM9l0FiiC9Bb5yAy6E5bd1iv7z4yqEf6J7g+xJ/LN+eYHsLwVPNtXcufRRLkUz6reLALpaG+GyXS8vNZRqQMs2jg5PCZdMbbsIZp7oZMhHgkjchHuGazdqFNZFnhgumLOfBh589GSKukZmDjREqJbJpSjtRHe75ZVW81LZF6p14dz5/VgjRK6GlU7o+RFCAu7o/VaJpFCjyjFC52UYv1LYAr7CRqH03iwtvhnpltM2GK/ShNkCYefQVCqQKBgQDbTb+yLLWYu8DFmF5v5syIPb3d9CQq4saHybgEfilVt/3/5QGPHUdW5Yby/nbQ/jtT/SkWGIyBcf6P603+M5T4TnPUq/fB0+EcR1GF1ztAN1kYwBNM1A2OCs0KIG8D8m1cwG/x6yY0V/vqUDAyN3IEGzE394JEg+h4sCNsYkxnJQKBgQC9BpJBAe8i+sEn3+ru+O6HZ5Q0GqlsWjnDEkT7sMl/SVnLvMsWgr+sMtniWwxhyxWF+iAG+123zgZMmwoBE3D+q8UFrVnuFGvhfbon/+VxCHDDMCiQ9+aU0qlfKOBe+Y53533uenu2XVGGPy4Tnaa6cEUTm2r1kSZ8xwupZdsz6wKBgDUcn1CHg3N6BTXVsQlo3CAi09jtR9UbFvnL4MU1yMz5woo27Cm87YoeDJDND/mNmNC+fzTavSycbwpr8neeBnYcifD3tQk8R0iR/Sxs8+yZevqDiikRMjc0ZnQNZtWN4O6VrIbqbFZA+MHGqeV/iB12eHUVfHcbezG/dtTI+bgBAoGAN+552EJ6QB9yiBn5qjx+WBKOB9zbxPPto4sEXnUxKjjKGT8D2OVXUdy6HABU6ZruA3a/g1FdqcWMl55Il4jpJnItGkDxG0FvqolJTAysF4Yn15moZzzzPZYYB9BQk8nEHjahTK9xa8SxGsgXGjbL3t7ZkbOrMn6ApOtAyB8bfYECgYEA03fP5b2Avrgv+p788Rgf8G75cZisX4IRP8p6OmdHzehBczw5gMLbIHmlYhEInRUOy5qh3w1tBvkOQm3WbGXcYrwN6Fg8lDFOlfgchnGKNb7t0eU+bEuODVbRc8UK7fqaqwNvJsx7Yu719TVfYMHqdGYaAEVFnlb6gBeeObD/xw0=";
	//返回格式
	public static final String FORMAT = "json";
	//编码
	public static final String CHARSET = "UTF-8";
	//支付宝公钥
	public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoe4HoxDokQ3pTlj9jC4SugjrlCSfICsiwPfc5i5NMh3uqDm6hQFTjyt4J9pj7CkBAEYqPQNeBDIs7Io2luSfWpInpOQfOFo20Bw5IJbtJORty/taGvbYe+lMyUPLKjJgZJKU1da+BAZqt70pTOmkiaHdJwRDCpvBM74SsAhLxSX/B6tUF3FDEwRDiN2fnqIMaBBdQP8OKrp7jmbl956rar6Y/20YObMraVgfNSkCb5fUpqzCB0dmbmMM3KTZVQHlfXnGczU6jhFmtjyvuWfVY27eK7Qg3Hzu/BmG+rpW4Xiqv7zHfKKI4bS3U/R6KjokAw3pifd/3bg34QaOSlYN9wIDAQAB";
	//RSA2
	public static final String SIGNTYPE = "RSA2";
	//服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static final String notify_url ="http://www.d1sc.com/app_alipay_notify.htm";
	//支付成功之后页面跳转同步通知页面路径 需http//或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
	public static final String return_url ="http://www.d1sc.com/h5PPPaySuccess.htm";
	public static final String aLIWapPay(HttpServletRequest httpRequest,
					JSONObject jsonobj){
		AlipayClient alipayClient = new DefaultAlipayClient(RQUEST_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGNTYPE); //获得初始化的AlipayClient
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
		alipayRequest.setNotifyUrl(notify_url);//在公共参数中设置回跳和通知地址
		alipayRequest.setReturnUrl(return_url);
		alipayRequest.setBizContent(jsonobj.toJSONString());//填充业务参数
		String form="";
		try{
			form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
		} catch (Exception e) {
			e.printStackTrace();
		}
		return form;
	}
}
