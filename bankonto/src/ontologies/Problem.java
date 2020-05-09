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


public class Problem implements Concept{
// -------------------------------------

   private int num;
   private String msg;

   public int getNum() {
      return this.num;
   }

   public String getMsg() {
      return this.msg;
   }

   public void setNum(int num) {
      this.num = num;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }
}
