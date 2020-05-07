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
/********************************************************************************
* BankOntology:
* -------------
* This class is an example showing how to use JADE content language support  ,
* to define an application ontology.
*
* Version 1.0 - July 2003
* Author: Ambroise Ncho, under the supervision of Professeur Jean Vaucher.
*
* Universite de Montreal - DIRO
*
*********************************************************************************/


import jade.content.onto.*;
import jade.content.schema.*;


public class BankOntology extends Ontology implements BankVocabulary {

   // ----------> The name identifying this ontology
   public static final String ONTOLOGY_NAME = "Bank-Ontology";

   // ----------> The singleton instance of this ontology
   private static Ontology instance = new BankOntology();

   // ----------> Method to access the singleton ontology object
   public static Ontology getInstance() { return instance; }


   // Private constructor
   private BankOntology() {

      super(ONTOLOGY_NAME, BasicOntology.getInstance());

      try {

         // ------- Add Concepts

         // Account
         ConceptSchema cs = new ConceptSchema(ACCOUNT);
         add(cs, Account.class);
         cs.add(ACCOUNT_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
         cs.add(ACCOUNT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
         cs.add(ACCOUNT_BALANCE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);

         // Problem
         add(cs = new ConceptSchema(PROBLEM), Problem.class);
         cs.add(PROBLEM_NUM, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
         cs.add(PROBLEM_MSG, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

         // Operation
         add(cs = new ConceptSchema(OPERATION), Operation.class);
         cs.add(OPERATION_TYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
         cs.add(OPERATION_AMOUNT, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(OPERATION_BALANCE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(OPERATION_ACCOUNTID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
         cs.add(OPERATION_DATE, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.MANDATORY);

         // ------- Add AgentActions

         // CreateAccount
         AgentActionSchema as = new AgentActionSchema(CREATE_ACCOUNT);
         add(as, CreateAccount.class);
         as.add(CREATE_ACCOUNT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

         // MakeOperation
         add(as = new AgentActionSchema(MAKE_OPERATION), MakeOperation.class);
         as.add(MAKE_OPERATION_TYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
         as.add(MAKE_OPERATION_AMOUNT, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         as.add(MAKE_OPERATION_ACCOUNTID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

         // Information
         add(as = new AgentActionSchema(INFORMATION), Information.class);
         as.add(INFORMATION_TYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
         as.add(INFORMATION_ACCOUNTID, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
      }
      catch (OntologyException oe) {
         oe.printStackTrace();
      }
   }
}// BankOntology
