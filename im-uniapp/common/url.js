let isHttpURL = (url) => {
	//判断URL地址的正则表达式为:http(s)?://([\w-]+\.)+[\w-]+(/[\w- ./?%&=]*)?
	//下面的代码中应用了转义字符"\"输出一个字符"/"
	let Expression = /http(s)?:\/\/([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/;
	let objExp = new RegExp(Expression);
	return objExp.test(url);
}

let getQueryParams = (url) => {
	const queryParams = {};
	const regExp = /[?&]+([^=&]+)=([^&]*)/g;
	url.replace(regExp, (match, key, value) => {
		queryParams[key] = decodeURIComponent(value);
	});
	return queryParams;
}

let replaceURLWithHTMLLinks = (content, color) => {
	// 使用正则表达式匹配更广泛的URL格式(此正则由deepseek生成)
	const urlRegex =
		/(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|]|\bwww\.[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
	return content.replace(urlRegex, (url) => {
		// 如果URL不以http(s)://开头，则添加http://前缀
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		return `<a href="${url}" target="_blank" style="color: ${color};text-decoration: underline;">${url}</a>`;
	});
}


export default {
	isHttpURL,
	getQueryParams,
	replaceURLWithHTMLLinks
}