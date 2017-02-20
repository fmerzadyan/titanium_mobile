/* 
 * @out is output of concerned function
 * @msg is the message to display describing @out
 * @flag is priority level of message. default is debug
 */
function log(out, msg, flag) {
	if (msg == undefined) {
		msg = " ";
	} else {
		msg = " " + msg + " ";
	}
	if (flag == "e" || flag == "error") {
		Ti.API.error("test msg error" + msg + out);
		return;
	}
	Ti.API.debug("test msg" + msg + out);
}

var win = Ti.UI.createWindow({
	title: "classic_app",
	backgroundColor: "#00FF00"
});
try {
	var drawer = Ti.UI.createDrawer({
		drawerItems:
		[
			{
				title: "bill",
				icon: "adventure_1"
			},
			{
				title: "ted",
				icon: "adventure_2"
			}
		]
	});
	win.add(drawer);
} catch (err) {
	log(err, "error:", "e");
}
win.open();