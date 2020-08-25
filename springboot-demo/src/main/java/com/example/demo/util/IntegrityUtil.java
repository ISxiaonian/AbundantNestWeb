package com.example.demo.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.mks.api.Command;
import com.mks.api.FileOption;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.SelectionList;
import com.mks.api.response.APIException;
import com.mks.api.response.Field;
import com.mks.api.response.Item;
import com.mks.api.response.ItemList;
import com.mks.api.response.Response;
import com.mks.api.response.Result;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;


public class IntegrityUtil {

    private static Log log = LogFactory.getLog(IntegrityUtil.class);
    private static IntegrityUtil instance;
    private static Connection conn = (Connection) Caches.map.get(Constants.CONECTION_KEY);

    public static final FileFilter filesOnly = new FileFilter() {
        public boolean accept(File file) {
            return file.isFile();
        }
    };

    private IntegrityUtil() {
    }

    public static synchronized IntegrityUtil getInstance() {
        if (instance == null) {
            instance = new IntegrityUtil();
        }
        return instance;
    }

    public static String getMessage(APIException ae) {
        String message = ae.getMessage();
        Response aeresp = ae.getResponse();
        if (aeresp != null) {
            WorkItemIterator wit = aeresp.getWorkItems();
            try {
                while (wit.hasNext()) {
                    wit.next();
                }
            } catch (APIException aenested) {
                String curMessage = aenested.getMessage();
                if (curMessage != null) {
                    message = curMessage;
                }
            }
        }
        return message;
    }

    public static String getResult(Response res) {
        try {
            if (res.getResult() != null) {
                return res.getResult().getMessage();
            }
            WorkItemIterator wit = res.getWorkItems();
            while (wit.hasNext()) {
                WorkItem wi = wit.next();
                if (wi.getResult() != null) {
                    return wi.getResult().getMessage();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return "";
    }

    public String createItem(String type, Hashtable<String, String> itemData) throws APIException {
        String id = null;

        Command cmd = new Command("im", "createissue");
        cmd.addOption(new Option("displayIdOnly"));
        cmd.addOption(new Option("type", type));
        for (String fieldName : itemData.keySet()) {
            if (fieldName.equalsIgnoreCase("ID")) {
                log.warn("Cannot modify 'ID' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Created Date")) {
                log.warn("Cannot modify 'Created Date' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Created By")) {
                log.warn("Cannot modify 'Created By' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Type")) {
                log.warn("Cannot modify 'Type' field for the create issue command!  Ignoring mapping!");
            } else {
                String fieldValue = itemData.get(fieldName);
                if (Caches.attachmentFields.contains(fieldName)) {
                    List<Option> attachmentOptions = getItemAttachments(fieldValue, fieldName);
                    if (attachmentOptions != null) {
                        for (Option opt : attachmentOptions) {
                            cmd.addOption(opt);
                        }
                    }
                } else {
                    MultiValue mv = new MultiValue("=");
                    mv.add(fieldName);
                    mv.add(fieldValue);
                    if (Caches.richContentFields.contains(fieldName)) {
                        cmd.addOption(new Option("richContentField", mv));
                    } else {
                        cmd.addOption(new Option("field", mv));
                    }
                }
            }
        }

        Response response = conn.execute(cmd);
        if (response != null) {
            Result result = response.getResult();
            if (result != null) {
                Item item = result.getPrimaryValue();
                if (item != null) {
                    id = item.getId();
                    log.debug(result.getMessage());
                }
            }
        } else {
            log.error("Could not get Result from command");
        }
        return id;
    }

    public String createSegment(String type, Map<String, String> itemData) throws APIException {
        Hashtable<String, String> hashData = new Hashtable<String, String>();
        for (String fieldName : itemData.keySet()) {
            String fieldValue = itemData.get(fieldName);
            hashData.put(fieldName, fieldValue);
        }
        return createSegment(type, hashData);
    }

    public String createSegment(String type, Hashtable<String, String> itemData) throws APIException {
        String id = null;

        Command cmd = new Command("im", "createsegment");
        cmd.addOption(new Option("type", type));
        for (String fieldName : itemData.keySet()) {
            if (fieldName.equalsIgnoreCase("ID")) {
                log.warn("Cannot modify 'ID' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Created Date")) {
                log.warn("Cannot modify 'Created Date' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Created By")) {
                log.warn("Cannot modify 'Created By' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Type")) {
                log.warn("Cannot modify 'Type' field for the create issue command!  Ignoring mapping!");
            } else {
                String fieldValue = itemData.get(fieldName);
                MultiValue mv = new MultiValue("=");
                mv.add(fieldName);
                mv.add(fieldValue);
                cmd.addOption(new Option("field", mv));
            }
        }

        Response response = conn.execute(cmd);
        if (response != null) {
            Result result = response.getResult();
            if (result != null) {
                Item item = result.getPrimaryValue();
                if (item != null) {
                    id = item.getId();
                    log.debug("Result: " + result.getMessage());
                }
            }
        } else {
            log.error("Could not get Result from command");
        }
        return id;
    }

    public String createContent(String type, String parentID, String insertLocation, Map<String, String> itemData) throws APIException {
        Hashtable<String, String> hashData = new Hashtable<String, String>();
        for (String fieldName : itemData.keySet()) {
            String fieldValue = itemData.get(fieldName);
            hashData.put(fieldName, fieldValue);
        }
        return createContent(type, parentID, insertLocation, hashData);
    }

    public String createContent(String type, String parentID, String insertLocation, Hashtable<String, String> itemData) throws APIException {
        if (StringUtil.isEmpty(parentID)) {
            throw new APIException("createContent API Exception: parentID is [" + parentID + "]");
        }
        String id = null;

        Command cmd = new Command("im", "createcontent");
        cmd.addOption(new Option("type", type));
        cmd.addOption(new Option("parentID", parentID));
        if (StringUtil.isNotEmpty(insertLocation)) {
            cmd.addOption(new Option("insertLocation", insertLocation));
        }
        for (String fieldName : itemData.keySet()) {
            if (fieldName.equalsIgnoreCase("ID")) {
                log.warn("Cannot modify 'ID' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Created Date")) {
                log.warn("Cannot modify 'Created Date' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Created By")) {
                log.warn("Cannot modify 'Created By' field for the create issue command!  Ignoring mapping!");
            } else if (fieldName.equalsIgnoreCase("Type")) {
                log.warn("Cannot modify 'Type' field for the create issue command!  Ignoring mapping!");
            } else {
                String fieldValue = itemData.get(fieldName);
                if (Caches.attachmentFields.contains(fieldName)) {
                    List<Option> attachmentOptions = getItemAttachments(fieldValue, fieldName);
                    if (attachmentOptions != null) {
                        for (Option opt : attachmentOptions) {
                            cmd.addOption(opt);
                        }
                    }
                } else {
                    MultiValue mv = new MultiValue("=");
                    mv.add(fieldName);
                    mv.add(fieldValue);
                    if (Caches.richContentFields.contains(fieldName)) {
                        cmd.addOption(new Option("richContentField", mv));
                    } else {
                        cmd.addOption(new Option("field", mv));
                    }
                }
            }
        }

        Response response = conn.execute(cmd);
        if (response != null) {
            Result result = response.getResult();
            if (result != null) {
                Item item = result.getPrimaryValue();
                if (item != null) {
                    id = item.getId();
                    log.debug("Result: " + result.getMessage());
                }
            }
        } else {
            log.error("Could not get Result from command");
        }
        return id;
    }

    public boolean editItem(String itemID, Map<String, String> itemData) throws APIException {
        Hashtable<String, String> hashData = new Hashtable<String, String>();
        for (String fieldName : itemData.keySet()) {
            String fieldValue = itemData.get(fieldName);
            hashData.put(fieldName, fieldValue);
        }
        return editItem(itemID, hashData);
    }

    public boolean editItem(String itemID, Hashtable<String, String> itemData) throws APIException {
        Command cmd = new Command("im", "editissue");
        for (String fieldName : itemData.keySet()) {
            if (!fieldName.equalsIgnoreCase("ID")) {
                if (fieldName.equalsIgnoreCase("Created Date")) {
                    log.warn("Cannot modify 'Created Date' field for the edit issue command!  Ignoring mapping!");
                } else if (fieldName.equalsIgnoreCase("Created By")) {
                    log.warn("Cannot modify 'Created By' field for the edit issue command!  Ignoring mapping!");
                } else if (fieldName.equalsIgnoreCase("Type")) {
                    log.warn("Cannot modify 'Type' field for the edit issue command!  Ignoring mapping!");
                } else if (fieldName.equalsIgnoreCase(Constants.REMOVE_FIELD_VALUES)) {
                    String removeFieldValues = itemData.get(Constants.REMOVE_FIELD_VALUES);
                    if (StringUtil.isNotEmpty(removeFieldValues)) {
                        cmd.addOption(new Option(Constants.REMOVE_FIELD_VALUES, removeFieldValues));
                    }
                } else {
                    String fieldValue = itemData.get(fieldName);
                    if (Caches.attachmentFields.contains(fieldName)) {
                        List<Option> attachmentOptions = getItemAttachments(fieldValue, fieldName);
                        if (attachmentOptions != null) {
                            for (Option opt : attachmentOptions) {
                                cmd.addOption(opt);
                            }
                        }
                    } else if (Caches.relationshipFields.contains(fieldName)) {
                        Option relationshipOption = new Option("addFieldValues");
                        MultiValue mv = new MultiValue("=");
                        mv.add(fieldName);
                        mv.add(fieldValue);
                        relationshipOption.add(mv);
                        cmd.addOption(relationshipOption);
                    } else {
                        MultiValue mv = new MultiValue("=");
                        mv.add(fieldName);
                        mv.add(fieldValue);
                        if (Caches.richContentFields.contains(fieldName)) {
                            cmd.addOption(new Option("richContentField", mv));
                        } else {
                            cmd.addOption(new Option("field", mv));
                        }
                    }
                }
            }
        }

        cmd.addSelection(itemID);

        Response response = conn.execute(cmd);
        if ((response != null) && (response.getExitCode() == 0)) {
            log.debug("Response: " + response.getWorkItem(itemID).getResult().getMessage());

            return true;
        }
        log.error("Failed to edit item: " + itemID);

        return false;
    }

    private List<Option> getItemAttachments(String attachmentValue, String attachmentField) {
        List<Option> attachmentsList = new ArrayList<Option>();
        if (attachmentValue.indexOf(';') > 0) {
            StringTokenizer fileTokens = new StringTokenizer(attachmentValue, ";");
            while (fileTokens.hasMoreTokens()) {
                String attachToken = fileTokens.nextToken().trim();
                File attachFile = new File(attachToken);
                if (attachFile.isDirectory()) {
                    File[] fileList = attachFile.listFiles(filesOnly);
                    for (int i = 0; i < fileList.length; i++) {
                        File attachment = fileList[i];
                        attachmentsList.add(getSingleAttachment(attachment, attachmentField));
                    }
                } else if ((attachFile.isFile()) && (attachFile.canRead())) {
                    attachmentsList.add(getSingleAttachment(attachFile, attachmentField));
                } else {
                    log.error("Attachment directory or file '" + attachToken + "' does not exist!");
                }
            }
        } else {
            File itemAttachmentDir = new File(attachmentValue);
            if (itemAttachmentDir.isDirectory()) {
                File[] fileList = itemAttachmentDir.listFiles(filesOnly);
                for (int i = 0; i < fileList.length; i++) {
                    File attachment = fileList[i];
                    attachmentsList.add(getSingleAttachment(attachment, attachmentField));
                }
            } else if ((itemAttachmentDir.isFile()) && (itemAttachmentDir.canRead())) {
                attachmentsList.add(getSingleAttachment(itemAttachmentDir, attachmentField));
            } else {
                log.error("Attachment directory or file '" + attachmentValue + "' does not exist!");
            }
        }
        return attachmentsList;
    }

    private Option getSingleAttachment(File attachment, String attachmentField) {
        if (attachment.getName().indexOf(',') >= 0) {
            File renamedFile = new File(attachment.getAbsolutePath().replace(',', '_'));
            attachment.renameTo(renamedFile);
            attachment = renamedFile;
        }
        Option attachOption = new Option("addAttachment");
        MultiValue mvAttachField = new MultiValue("=");
        MultiValue mvAttachPath = new MultiValue("=");
        MultiValue mvAttachName = new MultiValue("=");
        MultiValue mvAttachSummary = new MultiValue("=");
        mvAttachField.add("field");
        mvAttachField.add(attachmentField);
        mvAttachPath.add("path");
        mvAttachPath.add("remote://" + attachment.getAbsolutePath());
        mvAttachName.add("name");
        mvAttachName.add(attachment.getName());
        mvAttachSummary.add("summary");
        mvAttachSummary.add(attachment.getName());
        attachOption.setSeparator(",");
        attachOption.add(mvAttachField);
        attachOption.add(mvAttachPath);
        attachOption.add(mvAttachName);
        attachOption.add(mvAttachSummary);

        return attachOption;
    }

    public static Option getSingleRelationship(String relationshipField, String id) {
        Option relationshipOption = new Option("addRelationships");
        MultiValue mvValue = new MultiValue("=");
        mvValue.add(relationshipField + ":" + id);
        relationshipOption.setSeparator(",");
        relationshipOption.add(mvValue);

        return relationshipOption;
    }


    @SuppressWarnings("unchecked")
    public List<String> getAllPickValues(String field) throws APIException {
        List<String> visiblePicks = new ArrayList<String>();
        Command cmd = new Command("im", "fields");
        cmd.addOption(new Option("noAsAdmin"));
        cmd.addOption(new Option("fields", "picks"));
        cmd.addSelection(field);
        Response res = conn.execute(cmd);
        if (res == null) {
            log.error("Response was null!");
        } else {
            WorkItem wi = res.getWorkItem(field);
            if (wi != null) {
                Field picks = wi.getField("picks");
                List<Item> itemList = picks.getList();
                if (itemList != null) {
                    for (Item item : itemList) {
                        String visiblePick = item.getId();
                        if (!visiblePicks.contains(visiblePick)) {
                            visiblePicks.add(visiblePick);
                        }
                    }
                }
            }
        }
        return visiblePicks;
    }

    @SuppressWarnings("unchecked")
    public List<String> getActivePickValues(String field) throws APIException {
        List<String> visiblePicks = new ArrayList<String>();
        Command cmd = new Command("im", "viewfield");
        cmd.addSelection(field);
        Response res = conn.execute(cmd);
        if (res == null) {
            log.error("Response was null!");
        } else {
            WorkItem wi = res.getWorkItem(field);
            if (wi != null) {
                Field picks = wi.getField("picks");

                List<Item> itemList = picks.getList();
                if (itemList != null) {
                    Iterator<Field> it;
                    for (Item i : itemList) {
                        String pickName = i.getId();

                        it = i.getFields();
                        Field attribute = (Field) it.next();
                        if ((attribute != null) && (attribute.getName().equalsIgnoreCase("active"))
                                && (attribute.getValueAsString().equalsIgnoreCase("true"))
                                && (!visiblePicks.contains(pickName))) {
                            visiblePicks.add(pickName);
                        }
                    }
                }
            }
        }
        return visiblePicks;
    }

    public String removeContents(Set<String> ids) throws APIException {
        String result = null;
        if (ids == null) {
            return result;
        }
        log.info("remove list: " + ids);
        Command cmd = new Command("im", "removecontent");
        cmd.addOption(new Option("recurse"));
        cmd.addOption(new Option("noconfirm"));
        List<SelectionList> parallel = new ArrayList<SelectionList>();
        if (ids.size() > 500) {
            SelectionList sl = new SelectionList();
            Iterator<String> it = ids.iterator();
            int i = 0;
            while (it.hasNext()) {
                if (i % 500 == 0 && ids.size() > 0) {
                    parallel.add(sl);
                    sl = new SelectionList();
                }
                sl.add(it.next());
                if (i + 1 == ids.size()) {
                    parallel.add(sl);
                    break;
                }
                i++;
            }
        } else {
            SelectionList sl = new SelectionList();
            for (String id : ids) {
                sl.add(id);
            }
            parallel.add(sl);
        }
        for (SelectionList selectionList : parallel) {
            cmd.setSelectionList(selectionList);
            Response response = conn.execute(cmd);
            result = getResult(response);
        }
        return result;
    }

    public Item getItem(String id) throws APIException {
        Command cmd = new Command("im", "viewissue");
        cmd.addOption(new Option("substituteParams"));
        cmd.addSelection(id);

        Item item = null;
        Response response = conn.execute(cmd);
        if (response != null) {
            item = response.getWorkItem(id);
        } else {
            log.error("Could not get Result from command");
        }
        return item;
    }

    public List<Map<String, String>> findIssuesByQueryDef(List<String> fields, String query) throws APIException {
        if (conn == null) {
            throw new APIException("invoke findIssuesByQueryDef() ----- connection is null.");
        }
        if (query == null || query.isEmpty()) {
            throw new APIException("invoke findIssuesByQueryDef() ----- query is null or empty.");
        }
        if (fields == null) {
            fields = new ArrayList<String>();
        }
        if (fields.size() < 1) {
            fields.add("ID");
            fields.add("Project");
            fields.add("Type");
            fields.add("State");
        }
        MultiValue mv = new MultiValue(",");
        for (String field : fields) {
            mv.add(field);
        }
        Command command = new Command(Command.IM, Constants.ISSUES);
        command.addOption(new Option(Constants.FIELDS, mv));
        command.addOption(new Option(Constants.QUERY_DEFINITION, query));
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        while (it.hasNext()) {
            try {
                WorkItem wi = it.next();
                Iterator<?> iterator = wi.getFields();
                Map<String, String> map = new HashMap<String, String>();
                while (iterator.hasNext()) {
                    Field field = (Field) iterator.next();
                    String fieldName = field.getName();
                    if (Constants.ITEMLIST.equals(field.getDataType())) {
                        StringBuilder sb = new StringBuilder();
                        ItemList il = (ItemList) field.getList();
                        for (int i = 0; i < il.size(); i++) {
                            Item item = (Item) il.get(i);
                            if (i > 0) {
                                sb.append(",");
                            }
                            sb.append(item.getId());
                        }
                        map.put(fieldName, sb.toString());
                    } else {
                        map.put(fieldName, field.getValueAsString());
                    }
                }
                list.add(map);
            } catch (APIException e) {
                log.warn(getMessage(e));
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.API_EXCEPTION, Constants.API_EXCEPTION + ": " + getMessage(e));
                list.add(map);
            }
        }
        return list;
    }

    public Map<String, String> findissueById(String id) throws APIException {
        if (conn == null) {
            throw new APIException("invoke findissueById() ----- connection is null.");
        }
        if (id == null || id.isEmpty()) {
            throw new APIException("invoke findissueById() ----- id is null or empty.");
        }
        Command command = new Command(Command.IM, Constants.VIEW_ISSUE);
        command.addOption(new Option("showRichContent"));
        command.addOption(new Option("showTestResults"));
        command.addSelection(id);
        Response res = conn.execute(command);
        Map<String, String> map = new HashMap<String, String>();
        WorkItem wi = res.getWorkItem(id);
        Iterator<?> iterator = wi.getFields();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            String fieldName = field.getName();
            if (Constants.ITEMLIST.equals(field.getDataType())) {
                StringBuilder sb = new StringBuilder();
                ItemList il = (ItemList) field.getList();
                for (int i = 0; i < il.size(); i++) {
                    Item item = (Item) il.get(i);
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(item.getId());
                }
                map.put(fieldName, sb.toString());
            } else {
                map.put(fieldName, field.getValueAsString());
            }
        }
        return map;
    }

    public List<Map<String, String>> findIssuesByIds(List<String> fields, List<String> ids) throws APIException {
        if (conn == null) {
            throw new APIException("invoke findIssuesByIds() ----- connection is null.");
        }
        if (ids == null || ids.isEmpty()) {
            throw new APIException("invoke findIssuesByIds() ----- ids is null or empty.");
        }
        if (fields == null) {
            fields = new ArrayList<String>();
        }
        if (fields.size() < 1) {
            fields.add("ID");
            fields.add("Project");
            fields.add("Type");
            fields.add("State");
        }
        MultiValue mv = new MultiValue(",");
        for (String field : fields) {
            mv.add(field);
        }
        Command command = new Command(Command.IM, Constants.ISSUES);
        command.addOption(new Option(Constants.FIELDS, mv));
        SelectionList sl = new SelectionList();
        for (String id : ids) {
            sl.add(id);
        }
        command.setSelectionList(sl);
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        while (it.hasNext()) {
            try {
                WorkItem wi = it.next();
                Iterator<?> iterator = wi.getFields();
                Map<String, String> map = new HashMap<String, String>();
                while (iterator.hasNext()) {
                    Field field = (Field) iterator.next();
                    String fieldName = field.getName();
                    if (Constants.ITEMLIST.equals(field.getDataType())) {
                        StringBuilder sb = new StringBuilder();
                        ItemList il = (ItemList) field.getList();
                        for (int i = 0; i < il.size(); i++) {
                            Item item = (Item) il.get(i);
                            if (i > 0) {
                                sb.append(",");
                            }
                            sb.append(item.getId());
                        }
                        map.put(fieldName, sb.toString());
                    } else {
                        map.put(fieldName, field.getValueAsString());
                    }
                }
                list.add(map);
            } catch (APIException e) {
                log.warn(getMessage(e));
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.API_EXCEPTION, Constants.API_EXCEPTION + ": " + getMessage(e));
                list.add(map);
            }
        }
        return list;
    }

    public List<Map<String, String>> results(String sessionID) throws APIException {
        if (conn == null) {
            throw new APIException("invoke results() ----- connection is null.");
        }
        if (StringUtil.isEmpty(sessionID)) {
            throw new APIException("invoke results() ----- sessionID is null or empty.");
        }
        Command command = new Command(Command.TM, Constants.RESULTS);
        command.addOption(new Option("sessionID", sessionID));
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        while (it.hasNext()) {
            WorkItem wi = it.next();
            Iterator<?> iterator = wi.getFields();
            Map<String, String> map = new HashMap<String, String>();
            while (iterator.hasNext()) {
                Field field = (Field) iterator.next();
                String fieldName = field.getName();
                if (Constants.ITEMLIST.equals(field.getDataType())) {
                    StringBuilder sb = new StringBuilder();
                    ItemList il = (ItemList) field.getList();
                    for (int i = 0; i < il.size(); i++) {
                        Item item = (Item) il.get(i);
                        if (i > 0) {
                            sb.append(",");
                        }
                        sb.append(item.getId());
                    }
                    map.put(fieldName, sb.toString());
                } else {
                    map.put(fieldName, field.getValueAsString());
                }
            }
            list.add(map);
        }
        return list;
    }

    public Map<String, String> viewresult(String resultID) throws APIException {
        if (conn == null) {
            throw new APIException("invoke viewresult() ----- connection is null.");
        }
        if (StringUtil.isEmpty(resultID)) {
            throw new APIException("invoke viewresult() ----- resultID is null or empty.");
        }
        Map<String, String> map = new HashMap<String, String>();
        Command command = new Command(Command.TM, Constants.VIEW_RESULT);
        command.addSelection(resultID);
        Response res = conn.execute(command);
        WorkItem wi = res.getWorkItem(resultID);
        Iterator<?> iterator = wi.getFields();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            String fieldName = field.getName();
            if (Constants.ITEMLIST.equals(field.getDataType())) {
                StringBuilder sb = new StringBuilder();
                ItemList il = (ItemList) field.getList();
                for (int i = 0; i < il.size(); i++) {
                    Item item = (Item) il.get(i);
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(item.getId());
                }
                map.put(fieldName, sb.toString());
            } else {
                map.put(fieldName, field.getValueAsString());
            }
        }
        return map;
    }

    public Map<String, String> viewresult(String sessionID, String caseID) throws APIException {
        if (conn == null) {
            throw new APIException("invoke viewresult() ----- connection is null.");
        }
        if (StringUtil.isEmpty(sessionID)) {
            throw new APIException("invoke viewresult() ----- sessionID is null or empty.");
        }
        if (StringUtil.isEmpty(caseID)) {
            throw new APIException("invoke viewresult() ----- caseID is null or empty.");
        }
        Map<String, String> map = new HashMap<String, String>();
        Command command = new Command(Command.TM, Constants.VIEW_RESULT);
        command.addOption(new Option("sessionID", sessionID));
        command.addSelection(caseID);
        Response res = conn.execute(command);
        WorkItem wi = res.getWorkItem(sessionID + ":" + caseID);
        Iterator<?> iterator = wi.getFields();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            String fieldName = field.getName();
            if (Constants.ITEMLIST.equals(field.getDataType())) {
                StringBuilder sb = new StringBuilder();
                ItemList il = (ItemList) field.getList();
                for (int i = 0; i < il.size(); i++) {
                    Item item = (Item) il.get(i);
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(item.getId());
                }
                map.put(fieldName, sb.toString());
            } else {
                map.put(fieldName, field.getValueAsString());
            }
        }
        return map;
    }

    public SelectionList contains(SelectionList documents) throws APIException {
        return relationshipValues(Constants.CONTAINS, documents);
    }

    public SelectionList references(SelectionList referencedBies) throws APIException {
        return relationshipValues(Constants.REFERENCES, referencedBies);
    }

    public SelectionList relationshipValues(String fieldName, SelectionList ids) throws APIException {
        if (conn == null) {
            throw new APIException("invoke fieldValues() ----- connection is null.");
        }
        if (fieldName == null) {
            throw new APIException("invoke fieldValues() ----- fieldName is null.");
        }
        if (ids == null || ids.size() < 1) {
            throw new APIException("invoke fieldValues() ----- ids is null or empty.");
        }
        Command command = new Command(Command.IM, Constants.ISSUES);
        command.addOption(new Option(Constants.FIELDS, fieldName));
        command.setSelectionList(ids);
        Response res = conn.execute(command);
        WorkItemIterator it = res.getWorkItems();
        SelectionList contents = new SelectionList();
        while (it.hasNext()) {
            try {
                WorkItem wi = it.next();
                ItemList il = (ItemList) wi.getField(fieldName).getList();
                for (int i = 0; i < il.size(); i++) {
                    Item item = (Item) il.get(i);
                    String id = item.getId();
                    contents.add(id);
                }
            } catch (APIException e) {
                log.warn(getMessage(e));
            }
        }
        return contents;
    }

    public Map<String, String> expandContents(String document) throws APIException {
        if (StringUtil.isEmpty(document)) {
            throw new APIException("invoke expandTree() ----- documentIDs is null or empty.");
        }
        Map<String, String> map = new HashMap<String, String>();
        SelectionList sl = new SelectionList();
        sl.add(document);
        SelectionList contents = contains(sl);
        if (contents.size() > 0) {
            SelectionList contains = new SelectionList();
            contains.add(contents);
            while (true) {
                SelectionList conteins = contains(contains);
                if (conteins.size() < 1) {
                    break;
                }
                contents.add(conteins);
                contains = new SelectionList();
                contains.add(conteins);
            }

            log.info("Contains: " + contents.size());
            if (contents.size() > 500) {
                List<SelectionList> parallel = new ArrayList<SelectionList>();
                SelectionList ids = new SelectionList();
                for (int i = 0; ; i++) {
                    if (i % 500 == 0 && ids.size() > 0) {
                        parallel.add(ids);
                        ids = new SelectionList();
                    }
                    ids.add(contents.getSelection(i));
                    if (i + 1 == contents.size()) {
                        parallel.add(ids);
                        break;
                    }
                }
                for (SelectionList selectionList : parallel) {
                    log.info(selectionList);
                }
            } else {
                log.info(contents);
            }
        }

        return map;
    }

    public void extractAttachments(String id, String fieldName, String fileName, String outputFile)
            throws APIException {
        if (StringUtil.isEmpty(id)) {
            throw new APIException("invoke extractAttachments() ----- ID is null or empty.");
        }
        Command cmd = new Command(Command.IM, Constants.EXTRACT_ATTACHMENTS);
        cmd.addOption(new Option("field", fieldName));
        cmd.addOption(new Option("issue", id));
        cmd.addOption(new Option("overwriteExisting"));
        cmd.addOption(new FileOption("outputFile", outputFile));
        cmd.addSelection(fileName);
        Response res = conn.execute(cmd);
        APIException e = res.getAPIException();
        if (e != null) {
            throw e;
        }
    }

    public void addLabels(String label, String comment, Set<String> ids) throws APIException {
        if (StringUtil.isEmpty(label)) {
            throw new APIException("invoke addLabels() ----- label is null or empty.");
        }
        if (ids == null || ids.isEmpty()) {
            throw new APIException("invoke addLabels() ----- ids is null or empty.");
        }
        Command cmd = new Command(Command.IM, Constants.ADD_LABEL);
        cmd.addOption(new Option("label", label));
        if (StringUtil.isNotEmpty(comment)) {
            cmd.addOption(new Option("comment", comment));
        }
        for (String id : ids) {
            cmd.addSelection(id);
        }
        conn.execute(cmd);
    }

    public String viewType(String id) throws APIException {
        String type = null;
        if (StringUtil.isEmpty(id)) {
            throw new APIException("invoke viewType() ----- ID is null or empty.");
        }
        Command cmd = new Command(Command.IM, Constants.ISSUES);
        cmd.addOption(new Option(Constants.FIELDS, "Type"));
        cmd.addSelection(id);
        Response res = conn.execute(cmd);
        WorkItem wi = res.getWorkItem(id);
        type = wi.getField("Type").getValueAsString();
        return type;
    }

}
