package ru.max.echobot;

import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.max.bot.webhook.WebhookBot;
import ru.max.bot.webhook.WebhookBotOptions;
import ru.max.botapi.model.Update;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class WebhookEchoBot extends WebhookBot {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Options OPTIONS = new Options();

    private final EchoHandler handler;

    private WebhookEchoBot(String accessToken) {
        super(accessToken, WebhookBotOptions.DEFAULT);
        this.handler = new EchoHandler(getClient());
    }

    public static void main(String[] args) throws Exception {
        OptionSet optionSet;
        try {
            optionSet = OPTIONS.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        String accessToken = OPTIONS.accessToken.value(optionSet);
        WebhookEchoBot bot = new WebhookEchoBot(accessToken);

        int port = OPTIONS.port.value(optionSet);
        Tomcat tomcat = new Tomcat();
        tomcat.getService().addConnector(getSslConnector(port));

        Path base = Files.createTempDirectory("echobot");
        Context rootCtx = tomcat.addContext("", base.toAbsolutePath().toString());


        String serverUrl = OPTIONS.host.value(optionSet);
        if (OPTIONS.port.value(optionSet) != null) {
            serverUrl += ":" + OPTIONS.port.value(optionSet);
        }

        ServletWebhookBotContainer botContainer = new ServletWebhookBotContainer(serverUrl);
        botContainer.register(bot);

        String servletName = "Bots";
        Tomcat.addServlet(rootCtx, servletName, botContainer);
        rootCtx.addServletMappingDecoded("/bots/*", servletName);

        tomcat.start();
        botContainer.start();

        tomcat.getServer().await();
    }

    @Override
    public Object onUpdate(Update update) {
        LOG.info("Handling update: {}", update);
        update.visit(handler);
        return null;
    }

    private static Connector getSslConnector(int port) throws URISyntaxException {
        Connector connector = new Connector("HTTP/1.1");
        connector.setPort(port);
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("SSLEnabled", "true");
        connector.setAttribute("SSLProtocol", "TLSv1+TLSv1.1+TLSv1.2");
        connector.setAttribute("SSLCipherSuite",
                "ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA" +
                        "-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:kEDH+AESGCM:ECDHE-RSA" +
                        "-AES128-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE" +
                        "-RSA-AES256-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:ECDHE-ECDSA-AES256-SHA:DHE" +
                        "-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA256:DHE-RSA-AES256-SHA256:DHE-DSS" +
                        "-AES256-SHA:DHE-RSA-AES256-SHA:AES128-GCM-SHA256:AES256-GCM-SHA384:AES128-SHA256:AES256" +
                        "-SHA256:AES128-SHA:AES256-SHA:AES:CAMELLIA:DES-CBC3-SHA:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5" +
                        ":!PSK:!aECDH:!EDH-DSS-DES-CBC3-SHA:!EDH-RSA-DES-CBC3-SHA:!KRB5-DES-CBC3-SHA");

        Path crtFile = getPath("localhost.crt");
        Path keyFile = getPath("localhost.key");
        connector.setAttribute("SSLHonorCipherOrder", "true");
        connector.setAttribute("SSLDisableCompression", "true");
        connector.setAttribute("SSLCertificateFile", crtFile.toString());
        connector.setAttribute("SSLCertificateKeyFile", keyFile.toString());
        connector.setAttribute("SSLVerifyClient", "optional");
        return connector;
    }

    private static Path getPath(String name) throws URISyntaxException {
        URL resource = Objects.requireNonNull(WebhookEchoBot.class.getClassLoader().getResource(name), name);
        return Paths.get(resource.toURI()).toAbsolutePath();
    }

    private static class Options extends OptionParser {
        OptionSpec<String> accessToken = accepts("token")
                .withRequiredArg()
                .required()
                .ofType(String.class);

        OptionSpec<String> host = accepts("host")
                .withRequiredArg()
                .defaultsTo("0.0.0.0")
                .ofType(String.class);

        OptionSpec<Integer> port = accepts("port")
                .withRequiredArg()
                .ofType(Integer.class);
    }
}
