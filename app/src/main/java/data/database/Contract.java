package data.database;

/**
 * Created by Ahmad on 01/02/18.
 * All rights reserved.
 */

public class Contract {
    public static final class Note {
        public static final String table = "NOTE";
        public static final String id = "_id";
        public static final String typeId = "typeId";
        public static final String createDate = "createDate";
        public static final String modifyDate = "modifyDate";
        public static final String displayTitleFront = "displayTitleFront";
        public static final String displayDetailsFront = "displayDetailsFront";
        public static final String displayTitleBack = "displayTitleBack";
        public static final String displayDetailsBack = "displayDetailsBack";
        public static final String deleted = "deleted";

        public static final String idFull = table + "." + id;
        public static final String typeIdFull = table + "." + typeId;
        public static final String createDateFull = table + "." + createDate;
        public static final String modifyDateFull = table + "." + modifyDate;
        public static final String displayTitleFrontFull = table + "." + displayTitleFront;
        public static final String displayDetailsFrontFull = table + "." + displayDetailsFront;
        public static final String displayTitleBackFull = table + "." + displayTitleBack;
        public static final String displayDetailsBackFull = table + "." + displayDetailsBack;
        public static final String deletedFull = table + "." + deleted;
    }

    public static final class NoteData {
        public static final String table = "NOTE_DATA";
        public static final String id = "_id";
        public static final String noteId = "noteId";
        public static final String elementId = "elementId";
        public static final String groupId = "groupId";
        public static final String position = "position";
        public static final String pattern = "pattern";
        public static final String data1 = "data1";
        public static final String data2 = "data2";
        public static final String data3 = "data3";
        public static final String data4 = "data4";

        public static final String idFull = table + "." + id;
        public static final String noteIdFull = table + "." + noteId;
        public static final String elementIdFull = table + "." + elementId;
        public static final String groupIdFull = table + "." + groupId;
        public static final String positionFull = table + "." + position;
        public static final String patternFull = table + "." + pattern;
        public static final String data1Full = table + "." + data1;
        public static final String data2Full = table + "." + data2;
        public static final String data3Full = table + "." + data3;
        public static final String data4Full = table + "." + data4;
    }

    public static final class Type {

        public static final String table = "_TYPE";
        public static final String id = "_id";
        public static final String title = "title";
        public static final String color = "color";
        public static final String position = "position";
        public static final String isArchived = "isArchived";

        public static final String idFull = table + "." + id;
        public static final String titleFull = table + "." + title;
        public static final String colorFull = table + "." + color;
        public static final String positionFull = table + "." + position;
        public static final String isArchivedFull = table + "." + isArchived;
    }

    public static final class TypeElement {
        public static final String table = "TYPE_ELEMENT";
        public static final String id = "_id";
        public static final String typeId = "typeId";
        public static final String title = "title";
        public static final String position = "position";
        public static final String isArchived = "isArchived";
        public static final String sides = "sides";
        public static final String pattern = "patternId";
        public static final String initialCopy = "initialCopy";
        public static final String data1 = "data1";
        public static final String data2 = "data2";
        public static final String data3 = "data3";
        public static final String data4 = "data4";

        public static final String idFull = table + "." + id;
        public static final String typeIdFull = table + "." + typeId;
        public static final String titleFull = table + "." + title;
        public static final String positionFull = table + "." + position;
        public static final String isArchivedFull = table + "." + isArchived;
        public static final String sidesFull = table + "." + sides;
        public static final String patternFull = table + "." + pattern;
        public static final String initialCopyFull = table + "." + initialCopy;
        public static final String data1Full = table + "." + data1;
        public static final String data2Full = table + "." + data2;
        public static final String data3Full = table + "." + data3;
        public static final String data4Full = table + "." + data4;
    }

    public static final class Label {
        public static final String table = "LABEL";
        public static final String id = "_id";
        public static final String title = "title";
        public static final String deleted = "deleted";

        public static final String idFull = table + "." + id;
        public static final String titleFull = table + "." + title;
        public static final String deletedFull = table + "." + deleted;
    }

    public static final class LabelNote {
        public static final String table = "LABEL_NOTE";
        public static final String labelId = "labelId";
        public static final String noteId = "noteId";

        public static final String labelIdFull = table + "." + labelId;
        public static final String noteIdFull = table + "." + noteId;
    }

    public static final class LabelList {
        public static final String table = "LABEL_LIST";
        public static final String id = "_id";
        public static final String title = "title";
        public static final String color = "color";
        public static final String parentId = "parentId";
        public static final String position = "position";

        public static final String idFull = table + "." + id;
        public static final String titleFull = table + "." + title;
        public static final String colorFull = table + "." + color;
        public static final String parentIdFull = table + "." + parentId;
        public static final String positionFull = table + "." + position;
    }

    public static final class LabelListLabel {
        public static final String table = "LABEL_LIST_LABEL";
        public static final String labelId = "labelId";
        public static final String labelListId = "labelListId";
        public static final String position = "position";

        public static final String labelIdFull = table + "." + labelId;
        public static final String labelListIdFull = table + "." + labelListId;
        public static final String positionFull =  table + "." + position;
    }

    public static final class Schedule {
        public static final String table = "SCHEDULE";
        public static final String id = "_id";
        public static final String title = "title";
        public static final String color = "color";
        public static final String position = "position";

        public static final String idFull = table + "." + id;
        public static final String titleFrontFull = table + "." + title;
        public static final String colorFull = table + "." + color;
        public static final String positionFull = table + "." + position;
    }

    public static final class Occurrence {
        public static final String table = "OCCURRENCE";
        public static final String id = "_id";
        public static final String scheduleId = "scheduleId";
        /**
         * Starting at 0
         */
        public static final String number = "number";
        public static final String plusDays = "plusDays";

        public static final String idFull = table + "." + id;
        public static final String scheduleIdFull  =  table + "." + scheduleId;
        public static final String numberFull  =  table + "." + number;
        public static final String plusDaysFull  =  table + "." + plusDays;
    }

    public static final class ScheduleConversion {
        public static final String table = "SCHEDULE_CONVERSION";
        public static final String fromOccurrenceId = "fromOccurrenceId";
        public static final String toScheduleId = "toScheduleId";
        public static final String toOccurrenceNumber = "toOccurrenceNumber";

        public static final String fromOccurrenceIdFull = table + "." + fromOccurrenceId;
        public static final String toScheduleIdFull = table + "." + toScheduleId;
        public static final String toOccurrenceNumberFull = table + "." + toOccurrenceNumber;
    }

    public static final class RevisionPast {
        public static final String table = "REVISION_PAST";
        public static final String noteId = "noteId";
        public static final String date = "date";

        public static final String noteIdFull  =  table + "." + noteId;
        public static final String dateFull  =  table + "." + date;
    }

    public static final class RevisionFuture {
        public static final String table = "REVISION_FUTURE";
        public static final String noteId = "noteId";
        public static final String dueDate = "dueDate";
        public static final String scheduleId = "scheduleId";
        public static final String occurrenceNumber = "occurrenceNumber";

        public static final String noteIdFull  =  table + "." + noteId;
        public static final String dueDateFull  =  table + "." + dueDate;
        public static final String scheduleIdFull  =  table + "." + scheduleId;
        public static final String occurrenceNumberFull  =  table + "." + occurrenceNumber;
    }

    public static final class Picture {
        public static final String table = "PICTURE";
        public static final String pictureId = "pictureId";
        public static final String noteId = "noteId";
        public static final String pictureIdFull = table + "." + pictureId;
        public static final String noteIdFull = table + "." + noteId;
    }
}
