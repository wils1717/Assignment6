
package messages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

@ApplicationScoped
public class MessageController {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private List<Message> messages;

    public MessageController() {
        messages = new ArrayList<>();
    }

    public JsonArray getAllJson() {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Message m : messages) {
            json.add(m.toJson());
        }
        return json.build();
    }

    public Message getById(int id) {
        for (Message m : messages) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public JsonObject getByIdJson(int id) {
        Message m = getById(id);
        if (m != null) {
            return getById(id).toJson();
        } else {
            return null;
        }
    }

    public JsonArray getByDateJson(Date from, Date to) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Message m : messages) {
            if ((m.getSenttime().after(from) && m.getSenttime().before(to))
                    || m.getSenttime().equals(from) || m.getSenttime().equals(to)) {
                json.add(m.toJson());
            }
        }
        return json.build();
    }

    public JsonObject addJson(JsonObject json) {
        Message m = new Message(json);
        messages.add(m);
        return m.toJson();
    }

    public JsonObject editJson(int id, JsonObject json) {
        Message m = getById(id);
        m.setTitle(json.getString("title", ""));
        m.setContents(json.getString("contents", ""));
        m.setAuthor(json.getString("author", ""));
        String timeStr = json.getString("senttime", "");
        try {
            m.setSenttime(sdf.parse(timeStr));
        } catch (ParseException ex) {
            m.setSenttime(new Date());
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, "Failed Parsing Date: " + timeStr);
        }
        return m.toJson();
    }

    public boolean deleteById(int id) {
        Message m = getById(id);
        if (m != null) {
            messages.remove(m);
            return true;
        } else {
            return false;
        }
    }
}
