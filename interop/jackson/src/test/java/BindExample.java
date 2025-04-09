import net.notjustanna.webview.WebviewStandalone;
import net.notjustanna.webview.interop.JacksonWebviewInterop;

public class BindExample {
    public static void main(String[] args) {
        try(var webview = new WebviewStandalone(true)) {
            webview
                .setTitle("Webview - HTML5 Test")
                .setFixedSize(400, 300)
                .setHtml("<html><body><button onclick=\"hello()\">Hello</button></body></html>")
                .bringToFront();

            JacksonWebviewInterop interop = new JacksonWebviewInterop(webview.getWebview());
            interop.bind("hello", aargs -> {
                System.out.println("Received call from JavaScript");
                System.out.println("Arguments: " + aargs);
                return "Hello from Java!";
            });

            webview.run();
        }
    }
}
