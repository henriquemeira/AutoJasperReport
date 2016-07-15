package net.meira.util.AutoJasperReport;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class App {

        public static Connection getConnection(String host_servidor_banco, String nome_banco, String usuario_banco,
                        String senha_banco) throws SQLException {
                String url = "jdbc:postgresql://" + host_servidor_banco + "/" + nome_banco + "?user=" + usuario_banco
                                + "&password=";

                System.out.println("Conexao: " + url + "****");

                url = url + senha_banco;

                try {
                        Class.forName("org.postgresql.Driver");
                } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

                Connection conn = DriverManager.getConnection(url);
                return conn;
        }

        public static Map<String, Object> getParameters(String filial, String periodo) throws ParseException {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date dt1 = new Date(format.parse(periodo + "-01").getTime());

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dt1);

                calendar.add(2, 1);
                calendar.set(5, 1);
                calendar.add(5, -1);

                Date dt2 = calendar.getTime();

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("filial", filial);
                map.put("dt_inicial", dt1);
                map.put("dt_final", dt2);

                System.out.println("Parametros: ");
                System.out.println(" filial " + filial);
                System.out.println(" dt_inicial " + dt1);
                System.out.println(" dt_final " + dt2);

                return map;
        }

        public static JasperReport getJasperReport(String arquivo_jrxml) throws JRException {
                System.out.println("Relatorio fonte: " + arquivo_jrxml);
                return JasperCompileManager.compileReport(arquivo_jrxml);
        }

        private static void exportPdfFile(JasperPrint jp, String diretorio_destino, String filial, String periodo,
                        String sufixo_nome_arquivo) throws JRException {
                String arquivo_destino = diretorio_destino + "/" + lpad(filial, "0", 3) + "_" + periodo + "_"
                                + sufixo_nome_arquivo + ".pdf";
                System.out.println("PDF destino: " + arquivo_destino);

                try {
                        JasperExportManager.exportReportToPdfStream(jp, new FileOutputStream(arquivo_destino));
                } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }


        }

        public static void main(String[] args) throws JRException, SQLException, ParseException {

                Long start = new Date().getTime();

                System.out.println("Parametros: ");
                System.out.println("-x arquivo_jrxml");
                System.out.println("-f filial");
                System.out.println("-p periodo (yyyy-mm)");
                System.out.println("-d diretorio_destino");
                System.out.println("-s sufixo_nome_arquivo(.pdf)");
                System.out.println("-db_servidor host_servidor_banco");
                System.out.println("-db_nome nome_banco");
                System.out.println("-db_user usuario_banco");
                System.out.println("-db_senha senha_banco");

                System.out.println("");

                String arquivo_jrxml = null;
                String filial = null;
                String periodo = null;
                String diretorio_destino = null;
                String sufixo_nome_arquivo = null;
                String host_servidor_banco = null;
                String nome_banco = null;
                String usuario_banco = null;
                String senha_banco = null;

                for (int i = 0; i < args.length - 1; i++) {
                        if (args[i].equals("-x")) {
                                arquivo_jrxml = args[(i + 1)];
                        } else if (args[i].equals("-f")) {
                                filial = args[(i + 1)];
                        } else if (args[i].equals("-p")) {
                                periodo = args[(i + 1)];
                        } else if (args[i].equals("-d")) {
                                diretorio_destino = args[(i + 1)];
                        } else if (args[i].equals("-s")) {
                                sufixo_nome_arquivo = args[(i + 1)];
                        } else if (args[i].equals("-db_servidor")) {
                                host_servidor_banco = args[(i + 1)];
                        } else if (args[i].equals("-db_nome")) {
                                nome_banco = args[(i + 1)];
                        } else if (args[i].equals("-db_user")) {
                                usuario_banco = args[(i + 1)];
                        } else if (args[i].equals("-db_senha")) {
                                senha_banco = args[(i + 1)];
                        }
                }

                System.out.println("Valores dos Parametros: ");
                System.out.println("-x " + arquivo_jrxml);
                System.out.println("-f " + filial);
                System.out.println("-p " + periodo);
                System.out.println("-d " + diretorio_destino);
                System.out.println("-s " + sufixo_nome_arquivo);
                System.out.println("-db_servidor " + host_servidor_banco);
                System.out.println("-db_nome " + nome_banco);
                System.out.println("-db_user " + usuario_banco);
                System.out.println("-db_senha " + (senha_banco == null ? "null" : "****"));

                Map<String, Object> p = getParameters(filial, periodo);

                Connection c = getConnection(host_servidor_banco, nome_banco, usuario_banco, senha_banco);

                JasperReport jr = getJasperReport(arquivo_jrxml);
                JasperPrint jp = JasperFillManager.fillReport(jr, p, c);

                exportPdfFile(jp, diretorio_destino, filial, periodo, sufixo_nome_arquivo);

                System.out.println("Concluído em " + (new Date().getTime() - start) + " ms");
        }

        public static String lpad(String valueToPad, String filler, int size) {
                while (valueToPad.length() < size) {
                        valueToPad = filler + valueToPad;
                }
                return valueToPad;
        }
}
