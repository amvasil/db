/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ЮЛМАРТ
 */
public class JavaExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        // TODO code application logic here
        System.out.println("Hello world");
        String strURL = "jdbc:firebirdsql://localhost/C:\\db\\nanofon.fdb?lc_ctype=WIN1251&charSet=Cp1251";

	// Инициализируемя Firebird JDBC driver.
        // Эта строка действительна только для Firebird.
        try {

            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();

        } catch (Exception E) {
            System.err.println("Unable to load driver.");
        }
        Connection conn = null;
        //Создаём подключение к базе данных
        conn = DriverManager.getConnection(strURL, "SYSDBA", "masterkey");
        if (conn == null) {
            System.err.println("Could not connect to database");
            System.exit(-2);
        }

        Statement stmt = conn.createStatement();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("Мобильный оператор, выберите действие");
                System.out.println("1. Вывести стоимость звонков для всех тарифов");
                System.out.println("2. Пополнить баланс");
                System.out.println("3. Отправить SMS");
                System.out.println("4. Выход");
                int res = scanner.nextInt();
                if (res < 1 || res > 4) {
                    System.out.println("Неверный вариант\n");
                    continue;
                }
                if (res == 1) {
                    ResultSet rs;
                    rs = stmt.executeQuery("select tariffs.tariff_name,call_prices.price as calls from tariffs,call_prices,actions_tariffs act  where act.tariff=tariffs.id and act.action=call_prices.id;");
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "   " + rs.getString(2));
                    }
                } else if (res == 2) {
                    Long number;
                    System.out.println("Введите номер телефона (10 цифр)");
                    number = scanner.nextLong();
                    if (number < 9210000000L || number > 9219999999L) {
                        System.out.println("Неверный номер");
                        System.exit(-1);
                    }
                    System.out.println("Введите сумму в рублях:");
                    Integer amount;
                    amount = scanner.nextInt();
                    if (amount < 10 || amount > 10000) {
                        System.out.println("Недопустимая сумма");
                        System.exit(-1);
                    }
                    String str = String.format("insert into payments values (gen_id(gen_payid,1),(select id from subs where number = (select id from numbers where number=%d)),%d,current_timestamp)", number, amount);
                    System.out.println("Баланс пополнен");
                    stmt.executeUpdate(str);
                    str = String.format("update subs set balance = balance + %d where number = (select id from numbers where number = %d)", amount, number);
                    stmt.executeUpdate(str);
                } else if (res == 3) {
                    CallableStatement call_stmt = conn.prepareCall("{call sms(?,?)}");
                    Long number;
                    System.out.println("Введите ваш номер (10 цифр)");
                    number = scanner.nextLong();
                    if (number < 9210000000L || number > 9219999999L) {
                        System.out.println("Неверный номер");
                        System.exit(-1);
                    }
                    ResultSet rs;
                    String str = String.format("select id from subs where number=(select id from numbers where number=%d)",number);
                    rs = stmt.executeQuery(str);
                    rs.next();
                    System.out.println(rs.getString(1));
                    Integer sub_id = Integer.parseInt(rs.getString(1));
                    System.out.println(sub_id);
                    call_stmt.setInt(1, sub_id.intValue());
                    call_stmt.setInt(2, 1);
                    call_stmt.execute();
                } else if (res == 4) {
                    System.out.println("До свидания!");
                    System.exit(0);
                }
            } catch (InputMismatchException e) {
                System.out.println("Wrong input");
                System.exit(-1);
            }
        }


    }

}
