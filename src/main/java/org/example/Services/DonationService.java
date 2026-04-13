package org.example.Services;




import org.example.Entities.Association;
import org.example.Entities.Donation;
import org.example.Entities.User;
import org.example.Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DonationService {

    private final Connection cnx =
            MyConnection.getInstance().getConnection();

    // =====================================================
    // GET ALL DONATIONS WITH JOIN
    // =====================================================
    public List<Donation> getAllWithJoin() {

        List<Donation> list = new ArrayList<>();

        try {

            String sql = """
                SELECT d.*,
                       a.id          AS aid,
                       a.nom         AS anom,
                       a.region      AS aregion,

                       u.id          AS uid,
                       u.name        AS uname,
                       u.email       AS uemail,
                       u.phone       AS uphone,
                       u.region      AS uregion

                FROM donation d

                LEFT JOIN association a
                       ON d.association_id = a.id

                LEFT JOIN user u
                       ON d.donateur_id = u.id

                ORDER BY d.id DESC
            """;

            PreparedStatement ps =
                    cnx.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Donation d = new Donation();

                // =========================
                // DONATION
                // =========================
                d.setId(rs.getInt("id"));
                d.setType(rs.getString("type"));

                double montant = rs.getDouble("montant");
                if (!rs.wasNull()) {
                    d.setMontant(montant);
                }

                d.setDescriptionMateriel(
                        rs.getString("description_materiel")
                );

                d.setStatut(
                        rs.getString("statut")
                );

                d.setMessageDon(
                        rs.getString("message_don")
                );

                Timestamp ts =
                        rs.getTimestamp("date_don");

                if (ts != null) {
                    d.setDateDon(
                            ts.toLocalDateTime()
                    );
                }

                // =========================
                // ASSOCIATION
                // =========================
                if (rs.getObject("aid") != null) {

                    Association a =
                            new Association();

                    a.setId(
                            rs.getInt("aid")
                    );

                    a.setNom(
                            rs.getString("anom")
                    );

                    a.setRegion(
                            rs.getString("aregion")
                    );

                    d.setAssociation(a);
                }

                // =========================
                // USER
                // =========================
                if (rs.getObject("uid") != null) {

                    User u = new User();

                    u.setId(
                            rs.getInt("uid")
                    );

                    // 🔥 votre entity utilise getName/setName
                    u.setName(
                            rs.getString("uname")
                    );

                    u.setEmail(
                            rs.getString("uemail")
                    );

                    u.setPhone(
                            rs.getString("uphone")
                    );

                    u.setRegion(
                            rs.getString("uregion")
                    );

                    d.setDonateur(u);
                }

                list.add(d);
            }

            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =====================================================
    // ADD DONATION
    // =====================================================
    public void add(Donation d) {

        try {

            String sql = """
                INSERT INTO donation
                (
                    type,
                    montant,
                    description_materiel,
                    date_don,
                    statut,
                    message_don,
                    association_id,
                    donateur_id
                )
                VALUES (?,?,?,?,?,?,?,?)
            """;

            PreparedStatement ps =
                    cnx.prepareStatement(sql);

            ps.setString(1, d.getType());

            if (d.getMontant() != null)
                ps.setDouble(2, d.getMontant());
            else
                ps.setNull(2, java.sql.Types.DOUBLE);

            ps.setString(
                    3,
                    d.getDescriptionMateriel()
            );

            ps.setObject(
                    4,
                    d.getDateDon()
            );

            ps.setString(
                    5,
                    d.getStatut()
            );

            ps.setString(
                    6,
                    d.getMessageDon()
            );

            ps.setInt(
                    7,
                    d.getAssociation().getId()
            );

            if (d.getDonateur() != null)
                ps.setInt(
                        8,
                        1
                );
            else
                ps.setNull(
                        8,
                        java.sql.Types.INTEGER
                );

            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}