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

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.util.leap.*;

public class BankServerAgent extends Agent implements BankVocabulary {
// -------------------------------------------------------------------

   private int idCnt = 0;
   private Map accounts = new HashMap();
   private Map operations = new HashMap();

   protected void setup() {
// ------------------------

      // Set this agent main behaviour
      SequentialBehaviour sb = new SequentialBehaviour();
      sb.addSubBehaviour(new RegisterInDF(this));
      sb.addSubBehaviour(new ReceiveMessages(this));
      addBehaviour(sb);
   }

   class RegisterInDF extends OneShotBehaviour {
// ---------------------------------------------  Register in the DF for the client agent
//                                                be able to retrieve its AID
      RegisterInDF(Agent a) {
         super(a);
      }

      public void action() {

         ServiceDescription sd = new ServiceDescription();
         sd.setType(SERVER_AGENT);
         sd.setName(getName());
         sd.setOwnership("Prof6802");
         DFAgentDescription dfd = new DFAgentDescription();
         dfd.setName(getAID());
         dfd.addServices(sd);
         try {
            DFAgentDescription[] dfds = DFService.search(myAgent, dfd);
            if (dfds.length > 0 ) {
               DFService.deregister(myAgent, dfd);
            }
            DFService.register(myAgent, dfd);
            System.out.println(getLocalName() + " is ready.");
         }
         catch (Exception ex) {
            System.out.println("Failed registering with DF! Shutting down...");
            ex.printStackTrace();
            doDelete();
         }
      }
   }

   class ReceiveMessages extends CyclicBehaviour {
// -----------------------------------------------  Receive requests and queries from client
//                                                  agent and launch appropriate handlers

      public ReceiveMessages(Agent a) {

         super(a);
      }

      public void action() {

         ACLMessage msg = receive();
         if (msg == null) { block(); return; }
         try {
            Object content = msg.getContentObject();

            switch (msg.getPerformative()) {

               case (ACLMessage.REQUEST):

                  System.out.println("Request from " + msg.getSender().getLocalName());

                  if (content instanceof CreateAccount)
                     addBehaviour(new HandleCreateAccount(myAgent, msg));
                  else if (content instanceof MakeOperation)
                     addBehaviour(new HandleOperation(myAgent, msg));
                  else
                     replyNotUnderstood(msg);
                  break;

               case (ACLMessage.QUERY_REF):

                  System.out.println("Query from " + msg.getSender().getLocalName());

                  if (content instanceof Information)
                     addBehaviour(new HandleInformation(myAgent, msg));
                  else
                     replyNotUnderstood(msg);
                  break;

               default: replyNotUnderstood(msg);
            }
         }
         catch(Exception ex) { ex.printStackTrace(); }
      }
   }

   class HandleCreateAccount extends OneShotBehaviour {
// ----------------------------------------------------  Handler for a CreateAccount request

      private ACLMessage request;

      HandleCreateAccount(Agent a, ACLMessage request) {

         super(a);
         this.request = request;
      }

      public void action() {

         try {
            CreateAccount ca = (CreateAccount) request.getContentObject();
            Account acc = new Account();
            String id = generateId();
            acc.setId(id);
            acc.setName(ca.getName());
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContentObject(acc);
            send(reply);
            accounts.put(id, acc);
            operations.put(id, new ArrayList());
            System.out.println("Account created!");
         }
         catch(Exception ex) { ex.printStackTrace(); }
      }
   }

   class HandleOperation extends OneShotBehaviour {
// ------------------------------------------------  Handler for an Operation request

      private ACLMessage request;

      HandleOperation(Agent a, ACLMessage request) {

         super(a);
         this.request = request;
      }

      public void action() {

         try {
            MakeOperation mo = (MakeOperation) request.getContentObject();
            ACLMessage reply = request.createReply();
            Object obj = processOperation(mo);
            if (obj == null) replyNotUnderstood(request);
            else {
               reply.setPerformative(ACLMessage.INFORM);
               reply.setContentObject((java.io.Serializable)obj);
            }
            send(reply);
            System.out.println("Operation processed.");
         }
         catch(Exception ex) { ex.printStackTrace(); }
      }
   }

   class HandleInformation extends OneShotBehaviour {
// --------------------------------------------------  Handler for an Information query

      private ACLMessage query;

      HandleInformation(Agent a, ACLMessage query) {

         super(a);
         this.query = query;
      }

      public void action() {

         try {
            Information info = (Information) query.getContentObject();
            Object obj = processInformation(info);
            if (obj == null) replyNotUnderstood(query);
            else {
               ACLMessage reply = query.createReply();
               reply.setPerformative(ACLMessage.INFORM);
               reply.setContentObject((java.io.Serializable)obj);
            }
            //send(reply);
            System.out.println("Information processed.");
         }
         catch(Exception ex) { ex.printStackTrace(); }
      }
   }

   void replyNotUnderstood(ACLMessage msg) {
// -----------------------------------------

      try {
         java.io.Serializable content = msg.getContentObject();
         ACLMessage reply = msg.createReply();
         reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
         reply.setContentObject(content);
         send(reply);
      }
      catch(Exception ex) { ex.printStackTrace(); }
   }

   Object processOperation(MakeOperation mo) {
// --------------------------------------------

      Account acc = (Account)accounts.get(mo.getAccountId());
      if (acc == null) return newProblem(ACCOUNT_NOT_FOUND);
      if (mo.getAmount() <= 0) return newProblem(ILLEGAL_OPERATION);

      if (mo.getType() != DEPOSIT && mo.getType() != WITHDRAWAL)
         return null;
      if (mo.getType() == DEPOSIT)
         acc.setBalance(acc.getBalance() + mo.getAmount());
      else if (mo.getType() == WITHDRAWAL) {
         if (mo.getAmount() > acc.getBalance())
            return newProblem(NOT_ENOUGH_MONEY);
         acc.setBalance(acc.getBalance() - mo.getAmount());
      }
      Operation op = new Operation();  // <-- register the operation
      op.setAccountId(acc.getId());
      op.setType(mo.getType());
      op.setAmount(mo.getAmount());
      op.setBalance(acc.getBalance());
      op.setDate(new java.util.Date());
      List l = (List)operations.get(acc.getId());
      l.add(op);
      operations.put(acc.getId(), l);
      return acc;
   }

   Object processInformation(Information info) {
// ---------------------------------------------

      Account acc = (Account)accounts.get(info.getAccountId());
      if (acc == null) return newProblem(ACCOUNT_NOT_FOUND);

      java.util.Date date = new java.util.Date();
      Operation op = new Operation();              // <-- Apply admin charge
      op.setType(ADMIN);
      op.setAmount(info.getType()==BALANCE ? BAL_CHARGE : OPER_CHARGE);
      acc.setBalance(acc.getBalance() - op.getAmount());
      op.setBalance(acc.getBalance());
      op.setAccountId(acc.getId());
      op.setDate(date);
      List l = (List)operations.get(acc.getId());
      l.add(op);
      operations.put(acc.getId(), l);

      if (info.getType() == BALANCE) return acc;

      if (info.getType() == OPERATIONS) {
         OperationList opList = new OperationList();
         opList.setAccountId(info.getAccountId());
         opList.setOperations((List)operations.get(info.getAccountId()));
         return opList;
      }
      return null;
   }


//--------------------------- Utility methods ----------------------------//

   Problem newProblem(int num) {
// -----------------------------

      String msg = "";

      if (num == ACCOUNT_NOT_FOUND)
         msg = PB_ACCOUNT_NOT_FOUND;

      else if (num == NOT_ENOUGH_MONEY)
         msg = PB_NOT_ENOUGH_MONEY;

      else if (num == ILLEGAL_OPERATION)
         msg = PB_ILLEGAL_OPERATION;

      Problem prob = new Problem();
      prob.setNum(num);
      prob.setMsg(msg);
      return prob;
   }

   public String generateId() {
// ----------------------------

      return hashCode() + "" + (idCnt++);
   }
}


//=========================== External classes ===========================//

class Account implements java.io.Serializable {
// --------------------------------------------

   private String id;
   private String name;
   private float balance = 0;

   public String getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   public float getBalance() {
      return balance;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setBalance(float balance) {
      this.balance = balance;
   }

   public String toString() {
      return name + "  # " + id + "  --> balance = " + balance;
   }
}

class CreateAccount implements java.io.Serializable {
// --------------------------------------------------

   private String name;

   public String getName() {
     return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}

class Operation implements BankVocabulary, java.io.Serializable {
// -------------------------------------------------------------

   private int type;
   private float amount;
   private float balance;
   private String accountId;
   private java.util.Date date = null;

   public int getType() {
      return type;
   }

   public float getAmount() {
     return amount;
   }

   public float getBalance() {
     return balance;
   }

   public String getAccountId() {
      return accountId;
   }

   public java.util.Date getDate() {
     return date;
   }

   public void setType(int type) {
      this.type = type;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }

   public void setBalance(float balance) {
      this.balance = balance;
   }

   public void setAccountId(String accountId) {
      this.accountId = accountId;
   }

   public void setDate(java.util.Date date) {
      this.date = date;
   }

   public String getName() {
      if (type == DEPOSIT) return "Depos.";
      if (type == WITHDRAWAL) return "Withd.";
      return "Admin.";
   }

   public String toString() {
      return "\n\t" + date + "  " + getName() +
             "  " + amount + "  " + balance;
   }
}

class MakeOperation implements java.io.Serializable {
// --------------------------------------------------

   private String accountId;
   private int type;
   private float amount;

   public String getAccountId() {
      return accountId;
   }

   public int getType() {
      return type;
   }

   public float getAmount() {
     return amount;
   }

   public void setAccountId(String accountId) {
      this.accountId = accountId;
   }

   public void setType(int type) {
      this.type = type;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }
}

class OperationList implements java.io.Serializable {
// --------------------------------------------------

   private String accountId;
   private List operations;

   public String getAccountId() {
     return accountId;
   }

   public List getOperations() {
     return operations;
   }

   public void setAccountId(String accountId) {
      this.accountId = accountId;
   }

   public void setOperations(List operations) {
      this.operations = operations;
   }

   public String toString() {
      String s = "\n\tLIST OF OPERATIONS:";
      for (Iterator it = operations.iterator(); it.hasNext();) {
         Operation op = (Operation)it.next();
         s += op.toString();
      }
      return s;
   }
}

class Information implements java.io.Serializable {
// ------------------------------------------------

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

class Problem implements java.io.Serializable {
// --------------------------------------------

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
