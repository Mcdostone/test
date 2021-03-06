package io.chocorean.authmod.authentication.db;

import io.chocorean.authmod.model.IPlayer;
import io.chocorean.authmod.model.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayersDAO implements IPlayersDAO<IPlayer> {

    private final String table;
    private final IConnectionFactory connectionFactory;
    private final Map<String, String> columns;

    public PlayersDAO(String table, IConnectionFactory connectionFactory, Map<String, String> columns) throws SQLException {
        this.table = table;
        this.columns = columns;
        this.connectionFactory = connectionFactory;
        this.testTable();
    }

    private void testTable() throws SQLException {
        Connection conn = this.connectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(String.format("SELECT %s,%s,%s,%s,%s FROM %s",
                this.columns.get("email"),
                this.columns.get("banned"),
                this.columns.get("password"),
                this.columns.get("username"),
                this.columns.get("uuid"),
                this.table)
        );
        stmt.executeQuery();
        stmt.close();
    }

    @Override
    public void create(IPlayer player) throws SQLException {
        String query = String.format("INSERT INTO %s(%s, %s, %s, %s) VALUES(?, ?, ?, ?)",
                this.table,
                this.columns.get("email"),
                this.columns.get("password"),
                this.columns.get("username"),
                this.columns.get("uuid")
        );
        try(Connection conn = this.connectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.getEmail());
            stmt.setString(2, player.getPassword());
            stmt.setString(3, player.getUsername());
            if(player.getUuid() == null)
                stmt.setNull(4, Types.VARCHAR);
            else
                stmt.setString(4, player.getUuid());
            stmt.executeUpdate();
        }
    }

    @Override
    public Player findById(int id) throws SQLException {
        try(Connection conn = this.connectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE id = ?", this.table));
            ResultSet rs = stmt.executeQuery()) {
            stmt.setInt(1, id);
            return createPlayer(rs);
        }
    }

    @Override
    public List<IPlayer> findAll() throws SQLException {
        List<IPlayer> players = new ArrayList<>();
        try(Connection conn = this.connectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs  = stmt.executeQuery(String.format("SELECT * FROM %s", this.table))) {
            while(rs.next())
                players.add(this.createPlayer(rs, false));
        }
        return players;
    }

    @Override
    public Player findByEmail(String email) throws SQLException {
        try(Connection conn = this.connectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?", this.table, this.columns.get("email")))) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return createPlayer(rs);
        }
    }

    @Override
    public IPlayer findFirst(IPlayer player) throws SQLException {
        try(Connection conn = this.connectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? OR %s = ?",
                    this.table,
                    this.columns.get("email"),
                    this.columns.get("username")
            ))) {
            stmt.setString(1, player.getEmail());
            stmt.setString(2, player.getUsername());
            ResultSet rs = stmt.executeQuery();
            return createPlayer(rs);
        }
    }

    private Player createPlayer(ResultSet rs) throws SQLException {
        return createPlayer(rs, true);
    }

    private Player createPlayer(ResultSet rs, boolean closeAtEnd) throws SQLException {
        Player player = null;
        if(rs != null && rs.next()) {
            player = new Player();
            player.setBanned(rs.getInt(this.columns.get("banned")) != 0);
            player.setEmail(rs.getString(this.columns.get("email")));
            player.setPassword(rs.getString(this.columns.get("password")));
            player.setUsername(rs.getString(this.columns.get("username")));
            player.setUuid(rs.getString(this.columns.get("uuid")));
        }
        if(closeAtEnd && rs != null)
            rs.close();
        return player;
    }

}
