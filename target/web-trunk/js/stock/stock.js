var myVar = setInterval(function(){getNotice()}, 1000);
//var count = 0;
function getNotice(){
	$.get("http://localhost:8080/stock-info/get-notice",function(data,status){
		// var test = '[{"b1n":10,"b1v":100,"code":"800000","id":1,"isRead":0,"s1n":1000,"s1v":10,"stockName":"tb","time":100}]';
		var obj = data;
		if(obj!=null&&obj.length>0){
			for(var i=0;i<obj.length;i++){
				var notice = obj[i];
				if(notice.peType==0){
					$("div#parent").append('<div class=info-bs>'+
							'  股票代号: '+notice.code+', 股票名: '+notice.stockName+
							', 卖一价格: '+notice.s1v+', 卖一数量: '+notice.s1n+
							', 买一价格: '+notice.b1v+', 买一数量: '+notice.b1n+
							'</div>');
				}
				else{
					$("div#parent").append('<div class=info-pe>'+
							'  股票代号: '+notice.code+', 股票名: '+notice.stockName+
							', 卖一价格: '+notice.s1v+', 卖一数量: '+notice.s1n+
							', 买一价格: '+notice.b1v+', 买一数量: '+notice.b1n+
							'</div>');
				}
				var sound = '<audio autoplay="autoplay">  <source src="/sound/notice.mp3" type="audio/mp3" /> <source src="/sound/notice.wav" type="audio/wav" /> </audio>';
//				var sound = '<audio autoplay="autoplay">  <source src="http://www.w3school.com.cn/i/song.ogg" type="audio/ogg" /> <source src="http://www.w3school.com.cn/i/song.mp3" type="audio/mpeg" /> </audio>';
				$("body").append(sound);
//				console.log(notice);
			}
		}
	});
}
