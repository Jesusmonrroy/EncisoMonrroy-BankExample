/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontologies;

/**
 *
 * @author Jesus Enrique Enciso Monrroy
 */
import jade.content.*;


public class Information implements AgentAction {
// ----------------------------------------------

   private int type;
   private String accountId;

   public int getType() {
      return type;
   }

   public String getAccountId() {
      return accountId;
   }

   public void setType(int type) {
      this.type = type;
   }

   public void setAccountId(String accountId) {
      this.accountId = accountId;
   }
}
