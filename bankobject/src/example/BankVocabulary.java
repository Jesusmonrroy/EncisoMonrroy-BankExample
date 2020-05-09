/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

/**
 *
 * @author Jesus Enrique Enciso Monrroy
 */

public interface BankVocabulary {

   public static final int NEW_ACCOUNT = 1;
   public static final int DEPOSIT = 2;
   public static final int WITHDRAWAL = 3;
   public static final int BALANCE = 4;
   public static final int OPERATIONS = 5;
   public static final int ADMIN = 6;
   public static final int NOT_ENOUGH_MONEY = 10;
   public static final int ACCOUNT_NOT_FOUND = 11;
   public static final int ILLEGAL_OPERATION = 12;
   public final float BAL_CHARGE = (float)1.25;
   public final float OPER_CHARGE = (float)2.5;
   public static final String SERVER_AGENT = "Server agent";
   public static final String PB_ACCOUNT_NOT_FOUND = "Account not found";
   public static final String PB_NOT_ENOUGH_MONEY = "Not enough money";
   public static final String PB_ILLEGAL_OPERATION = "Illegal operation";

}