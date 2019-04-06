package org.turtledream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.File;

public class Main extends AbstractVerticle {

    @Override
    public void start(){
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));

        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end(
                    "<form action=\"/form\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                            "    <div>\n" +
                            "        <label for=\"name\">Select a file:</label>\n" +
                            "        <input type=\"file\" name=\"file\" multiple>\n" +
                            "    </div>\n" +
                            "    <div class=\"button\">\n" +
                            "        <button type=\"submit\">Send</button>\n" +
                            "    </div>" +
                            "</form>"
            );
        });

        router.route("/form").handler(ctx -> {

            ctx.response().setChunked(true);
            String html = "";

            for (FileUpload f : ctx.fileUploads()) {
                File file = new File(f.uploadedFileName());
                File fi = new File("uploads/" + f.fileName());
                if(fi.exists()) {
                    fi.delete();
                    file.renameTo(new File("uploads/" + f.fileName()));
                }
                else file.renameTo(new File("uploads/" + f.fileName()));
                file.delete();
            }

            File[] listOfFiles = new File("uploads/").listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    html = html + "<a href=\"/files/uploads/" + file.getName() + "\"><img src=\"/files/uploads/" + file.getName() + "\" style=\"width:200x;height:200px\" hspace=\"5px\" vspace=\"5px\" border = \"1px\"></a>\n";
                }
            }

            ctx.response().putHeader("Content-Type", "text/html").end(html);
        });

        router.route("/files/uploads/:image").handler(fh -> {
            fh.response().setChunked(true);
            Buffer uploadedFile = vertx.fileSystem().readFileBlocking("uploads/" + fh.request().getParam("image"));
            fh.response().write(uploadedFile);
            fh.response().putHeader("Content-Type", "image/*").end();
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }
}
