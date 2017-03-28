/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

/**
 *
 * @author c0533886
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        retrieveAllMessages();
    }

    public void retrieveAllMessages() {
        try {
            if (messages == null) {
                messages = new ArrayList<>();
            }
            messages.clear();
            Connection conn = DBUtils.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM message");
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setTitle(rs.getString("title"));
                m.setContents(rs.getString("contents"));
                m.setAuthor(rs.getString("author"));
                m.setSenttime(rs.getDate("senttime"));
                messages.add(m);
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void persistToDb(Message m) {
        try {
            String sql = "";
            Connection conn = DBUtils.getConnection();
            if (m.getId() <= 0) {
                sql = "INSERT INTO message (title, contents, author, senttime) VALUES (?, ?, ?, ?)";
            } else {
                sql = "UPDATE message SET title = ?, contents = ?, author = ?, senttime = ? WHERE id = ?";
            }
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, m.getTitle());
            pstmt.setString(2, m.getContents());
            pstmt.setString(3, m.getAuthor());
            pstmt.setDate(4, new java.sql.Date(m.getSenttime().getTime()));
            if (m.getId() > 0) {
                pstmt.setInt(5, m.getId());
            }
            pstmt.executeUpdate();
            if (m.getId() <= 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                rs.next();
                int id = rs.getInt(1);
                m.setId(id);
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeFromDb(Message m) {
        try {
            Connection conn = DBUtils.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM message WHERE id = ?");
            pstmt.setInt(1, m.getId());
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        persistToDb(m);
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
            // This sets the time to NOW if there's a failure parsing
            m.setSenttime(new Date());
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, "Failed Parsing Date: " + timeStr);
        }
        persistToDb(m);
        return m.toJson();
    }

    public boolean deleteById(int id) {
        Message m = getById(id);
        if (m != null) {
            removeFromDb(m);
            messages.remove(m);
            return true;
        } else {
            return false;
        }
    }
}

