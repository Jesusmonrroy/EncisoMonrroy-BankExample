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
/********************************************************************************
* BankClienAgent:
* ---------------
* This class is an example showing how to use the classes OneShotBehaviour,
* SimpleBehaviour, WakerBehaviour and ParallelBehaviour to program an agent.
*
* Version 3.0 - July 2003
* Author: Ambroise Ncho, under the supervision of Professeur Jean Vaucher
*
* Universite de Montreal - DIRO
*
*********************************************************************************/

import java.io.*;
import java.util.Date;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.util.leap.*;
import jade.gui.*;

import ontologies.*;


public class BankClientAgent extends GuiAgent implements BankVocabulary {
// ----------------------------------------------------------------------

   static final int WAIT = -1;
   static final int QUIT = 0;
   private int command = WAIT;
   private int cnt = 0;
   private AID server;
   private List accounts = new ArrayList();
   private Codec codec = new SLCodec();
   private Ontology ontology = BankOntology.getInstance();

   transient protected BankAgentGui myGui;  // The gui

   protected void setup() {
// ------------------------

      // Register language and ontology
      getContentManager().registerLanguage(codec);
      getContentManager().registerOntology(ontology);

      // Set up the gui
      myGui = new BankAgentGui(this);
      myGui.setVisible(true);
   }

   protected void takeDown() {
// ---------------------------  Terminate the program properly

       System.out.println(getLocalName() + " is now shutting down.");
       if (myGui!=null) {
          myGui.setVisible(false);
          myGui.dispose();
       }
    }

   protected void onGuiEvent(GuiEvent ev) {
// ----------------------------------------  Receive user command via the gui

      command = ev.getType();
      if (command == QUIT) {
         alertGui("Bye!");
         doDelete();
         System.exit(0);
      }
      if (command == NEW_ACCOUNT) {
         createAccount();
      }
      else if (command == DEPOSIT || command == WITHDRAWAL) {
         command = ev.getType();
         Account acc = (Account)ev.getParameter(0);
         float amount = ((Float)ev.getParameter(1)).floatValue();
         requestOperation(acc, amount);
      }
      else if (command == BALANCE || command == OPERATIONS) {
         Account acc = (Account)ev.getParameter(0);
         queryInformation(acc);
      }
   }

   void alertGui(Object response) {
// --------------------------------  Process the response of the server
//                                   to the gui for display
      myGui.alertResponse(response);
   }

   void resetStatusGui() {
// -----------------------  Reset the status of the gui
      myGui.resetStatus();
   }

   void createAccount() {
// ----------------------  Process to the server agent the request
//                         to create a new account

      CreateAccount ca = new CreateAccount();
      ca.setName("Account" + cnt++);
      sendMessage(ACLMessage.REQUEST, ca);
   }


   void requestOperation(Account acc, float amount) {
// --------------------------------------------------  Process to the server agent the
//                                                     request to make an operation
      MakeOperation mo = new MakeOperation();
      mo.setType(command);
      mo.setAmount(amount);
      mo.setAccountId(acc.getId());
      sendMessage(ACLMessage.REQUEST, mo);
   }

   void queryInformation(Account acc) {
// ------------------------------------   Process to the server agent the request
//                                        a query for information
      Information info = new Information();
      info.setType(command);
      info.setAccountId(acc.getId());
      sendMessage(ACLMessage.QUERY_REF, info);
   }


   class WaitServerResponse extends ParallelBehaviour {
// ----------------------------------------------------  launch a SimpleBehaviour to receive
//                                                       servers response and a WakerBehaviour
//                                                       to terminate the waiting if there is
//                                                       no response from the server
      WaitServerResponse(Agent a) {

         super(a, 1);

         addSubBehaviour(new ReceiveResponse(myAgent));

         addSubBehaviour(new WakerBehaviour(myAgent, 5000) {

            protected void handleElapsedTimeout() {
               alertGui("No response from server. Please, try later!");
               resetStatusGui();
            }
         });
      }
   }


   class ReceiveResponse extends SimpleBehaviour {
// -----------------------------------------------  // Receive and handle server responses

      private boolean finished = false;

      ReceiveResponse(Agent a) {
         super(a);
      }

      public void action() {

         ACLMessage msg = receive(MessageTemplate.MatchSender(server));

         if (msg == null) { block(); return; }

         if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD){
            alertGui("Response from server: NOT UNDERSTOOD");
         }
         else if (msg.getPerformative() != ACLMessage.INFORM){
            alertGui("\nUnexpected msg from server!");
         }
         else {
            try {
               ContentElement content = getContentManager().extractContent(msg);

               if (content instanceof Result) {

                  Result result = (Result) content;

                  if (result.getValue() instanceof Problem) {

                     Problem prob = (Problem)result.getValue();
                     alertGui(prob);
                  }
                  else if (result.getValue() instanceof Account) {

                     Account acc = (Account) result.getValue();

                     if (command == NEW_ACCOUNT) {
                        accounts.add(acc);
                     }
                     alertGui(acc);
                  }
                  else if (result.getValue() instanceof List) {
                     alertGui(result.getItems());
                  }
                  else alertGui("\nUnexpected result from server!");
               }
               else {
                  alertGui("\nUnable de decode response from server!");
               }
            }
            catch (Exception e) { e.printStackTrace(); }
         }
         resetStatusGui();
         finished = true;
      }

      public boolean done() { return finished; }

      public int onEnd() { command = WAIT; return 0; }
   }


   void lookupServer() {
// ---------------------  Search in the DF to retrieve the server AID

      ServiceDescription sd = new ServiceDescription();
      sd.setType(SERVER_AGENT);
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.addServices(sd);
      try {
         DFAgentDescription[] dfds = DFService.search(this, dfd);
         if (dfds.length > 0 ) {
            server = dfds[0].getName();
            alertGui("Localized server");
         }
         else alertGui("Unable to localize server. Please try later!");
      }
      catch (Exception ex) {
         ex.printStackTrace();
         System.out.println("Failed searching int the DF!");
      }
   }


//--------------------------- Utility methods ----------------------------//


   void sendMessage(int performative, AgentAction action) {
// --------------------------------------------------------
      if (server == null) lookupServer();
      if (server == null) {
         alertGui("Unable to localize the server! Operation aborted!");
         return;
      }
      ACLMessage msg = new ACLMessage(performative);
      msg.setLanguage(codec.getName());
      msg.setOntology(ontology.getName());
      try {
         getContentManager().fillContent(msg, new Action(server, action));
         msg.addReceiver(server);
         send(msg);
         alertGui("Contacting server... Please wait!");
         addBehaviour(new WaitServerResponse(this));
      }
      catch (Exception ex) { ex.printStackTrace(); }
   }

}
