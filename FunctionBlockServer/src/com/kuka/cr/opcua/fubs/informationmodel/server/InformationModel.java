/**
* Prosys OPC UA Java SDK
*
* Copyright (c) Prosys PMS Ltd., http://www.prosysopc.com.
* All rights reserved.
*/

/**
* DO NOT EDIT THIS FILE DIRECTLY! It is generated and will be overwritten on regeneration.
*/

package com.kuka.cr.opcua.fubs.informationmodel.server;

import com.prosysopc.ua.CodegenModel;
import com.kuka.cr.opcua.fubs.informationmodel.Serializers;
import com.prosysopc.ua.nodes.UaInstance;
import java.util.ArrayList;
import java.util.List;

public class InformationModel {
    
  private static List<Class<? extends UaInstance>> createClassesList(){
    ArrayList<Class<? extends UaInstance>> list = new ArrayList<Class<? extends UaInstance>>();
    list.add(MethodStatusUpdateEventNode.class);
    list.add(SemanticAnnotationTypeNode.class);
    list.add(FubProgramStateMachineTypeNode.class);
    list.add(VariableTypeNode.class);
    list.add(VarMetadataTypeNode.class);
    list.add(ViperVarGroupNode.class);
    list.add(SemanticReferenceTypeNode.class);
     return list;
    }

  public static final CodegenModel MODEL = new CodegenModel(
    createClassesList(), 
    Serializers.SERIALIZERS
  );
}