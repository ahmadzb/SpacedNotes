package data.xml.log.operator;

public class LogContract {

    public static final int StartLogIndex = 1;
    public static final int StartOperationId = 0;

    public static class LogRoot {
        public static final String root = "Log";
        public static final String fromOperationId = "fromOperationId";
        public static final String toOperationId = "toOperationId";
    }

    public static class Operation {
        public static final String itemName = "Operation";

        public static final String id = "id";
        public static final String time = "time";
        public static final String profileId = "forProfileId";
        public static final String operator = "operator";
    }

    public static class Operators {
        public static final String labelList = "labelList";
        public static final String label = "label";
        public static final String noteElement = "noteElement";
        public static final String typeElement = "typeElement";
        public static final String note = "note";
        public static final String type = "type";
        public static final String schedule = "schedule";
        public static final String revision = "revision";
        public static final String occurrence = "occurrence";
        public static final String picture = "picture";
        public static final String profile = "profile";
        public static final String existence = "existence";
    }

    public static class LabelList {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String title = "title";
            public static final String color = "color";
            public static final String parentId = "parentId";
            public static final String position = "position";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String title = "title";
            public static final String color = "color";
            public static final String parentId = "parentId";
            public static final String position = "position";
        }

        public static class UpdateLabels {
            public static final String itemName = "UpdateLabels";

            public static final String labelListId = "labelListId";

            public static class Label {
                public static final String itemName = "Label";

                public static final String id = "id";
            }
        }

        public static class UpdateLabelPosition {
            public static final String itemName = "UpdateLabelPosition";

            public static final String labelListId = "labelListId";
            public static final String labelId = "labelId";
            public static final String position = "position";
        }

        public static class UpdateLabelListPosition {
            public static final String itemName = "UpdateLabelListPosition";

            public static final String labelListId = "labelListId";
            public static final String position = "position";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }
    }

    public static class Label {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String title = "title";
            public static final String deleted = "deleted";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String title = "title";
            public static final String deleted = "deleted";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }

        public static class MarkAsDeleted {
            public static final String itemName = "MarkAsDeleted";

            public static final String id = "id";
            public static final String deletedDate = "deletedDate";
        }

        public static class MarkAsNotDeleted {
            public static final String itemName = "MarkAsNotDeleted";

            public static final String id = "id";
        }
    }

    public static class NoteElement {
        public static class UpdateNoteElements {
            public static final String itemName = "UpdateNoteElements";

            public static final String noteId = "noteId";

            public static class Element {
                public static final String itemName = "Element";

                public static final String typeElementId = "typeElementId";
                public static final String groupId = "groupId";
                public static final String position = "position";
                public static final String pattern = "pattern";


                public static class Text {
                    public static final String itemName = "Text";

                    public static final String dataId = "dataId";
                    public static final String text = "text";
                }

                public static class ListItem {
                    public static final String itemName = "ListItem";

                    public static final String dataId = "dataId";
                    public static final String text = "text";
                    public static final String secondText = "secondText";
                    public static final String position = "position";
                }

                public static class PictureItem {
                    public static final String itemName = "PictureItem";

                    public static final String dataId = "dataId";
                    public static final String pictureId = "pictureId";
                    public static final String position = "position";
                }

                public static class Divider {
                    public static final String itemName = "Divider";

                    public static final String dataId = "dataId";
                }
            }
        }

        public static class DeleteAllElementsByTypeElement {
            public static final String itemName = "DeleteAllElementsByTypeElement";

            public static final String typeElementId = "typeElementId";
        }

        public static class DeleteAllElementsByNote {
            public static final String itemName = "DeleteAllElementsByNote";

            public static final String noteId = "noteId";
        }
    }

    public static class Note {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String typeId = "typeId";
            public static final String createDate = "createDate";
            public static final String modifyDate = "modifyDate";
            public static final String displayTitleFront = "displayTitleFront";
            public static final String displayDetailsFront = "displayDetailsFront";
            public static final String displayTitleBack = "displayTitleBack";
            public static final String displayDetailsBack = "displayDetailsBack";
            public static final String deleted = "deleted";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String typeId = "typeId";
            public static final String createDate = "createDate";
            public static final String modifyDate = "modifyDate";
            public static final String displayTitleFront = "displayTitleFront";
            public static final String displayDetailsFront = "displayDetailsFront";
            public static final String displayTitleBack = "displayTitleBack";
            public static final String displayDetailsBack = "displayDetailsBack";
            public static final String deleted = "deleted";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }

        public static class MarkAsDeleted {
            public static final String itemName = "MarkAsDeleted";

            public static final String id = "id";
            public static final String deleted = "deleted";
        }

        public static class MarkAsNotDeleted {
            public static final String itemName = "MarkAsNotDeleted";

            public static final String id = "id";
        }

        public static class SetLabelToNote {
            public static final String itemName = "SetLabelToNote";

            public static final String noteId = "noteId";
            public static final String labelId = "labelId";
        }

        public static class UnsetLabelFromNote {
            public static final String itemName = "UnsetLabelFromNote";

            public static final String noteId = "noteId";
            public static final String labelId = "labelId";
        }

        public static class UnsetAllLabelsFromNote {
            public static final String itemName = "UnsetAllLabelsFromNote";

            public static final String noteId = "noteId";
        }
    }

    public static class Pictures {
        public static class SubmitPicture {
            public static final String itemName = "SubmitPicture";

            public static final String pictureId = "pictureId";
            public static final String noteId = "noteId";
        }

        public static class DeletePicture {
            public static final String itemName = "DeletePicture";

            public static final String pictureId = "pictureId";
        }
    }

    public static class Occurrence {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String number = "number";
            public static final String plusDays = "plusDays";
            public static final String scheduleId = "scheduleId";

            public static class Conversion {
                public static final String itemName = "Conversion";

                public static final String fromOccurrenceId = "fromOccurrenceId";
                public static final String toScheduleId = "toScheduleId";
                public static final String toOccurrenceNumber = "toOccurrenceNumber";
            }
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String number = "number";
            public static final String plusDays = "plusDays";
            public static final String scheduleId = "scheduleId";

            public static class Conversion {
                public static final String itemName = "Conversion";

                public static final String fromOccurrenceId = "fromOccurrenceId";
                public static final String toScheduleId = "toScheduleId";
                public static final String toOccurrenceNumber = "toOccurrenceNumber";
            }
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }
    }

    public static class Schedule {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String color = "color";
            public static final String title = "title";
            public static final String position = "position";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String color = "color";
            public static final String title = "title";
            public static final String position = "position";
        }

        public static class UpdatePosition {
            public static final String itemName = "UpdatePosition";

            public static final String id = "id";
            public static final String position = "position";
        }

        public static class MergeThenDelete {
            public static final String itemName = "MergeThenDelete";

            public static final String scheduleId = "scheduleId";
            public static final String newScheduleId = "newScheduleId";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }
    }

    public static class Revision {

        public static class BatchConvertRevisionFutures {
            public static final String itemName = "BatchConvertRevisionFutures";

            public static final String scheduleId = "scheduleId";
            public static final String newScheduleId = "newScheduleId";
        }

        public static class AddRevisionFuture {
            public static final String itemName = "AddRevisionFuture";

            public static final String noteId = "noteId";
            public static final String dueDate = "dueDate";
            public static final String scheduleId = "scheduleId";
            public static final String occurrenceNumber = "occurrenceNumber";
        }

        public static class UpdateRevisionFuture {
            public static final String itemName = "UpdateRevisionFuture";

            public static final String noteId = "noteId";
            public static final String dueDate = "dueDate";
            public static final String scheduleId = "scheduleId";
            public static final String occurrenceNumber = "occurrenceNumber";
        }

        public static class DeleteRevisionFuture {
            public static final String itemName = "DeleteRevisionFuture";

            public static final String noteId = "noteId";
        }

        public static class AddRevisionPast {
            public static final String itemName = "AddRevisionPast";

            public static final String noteId = "noteId";
            public static final String date = "date";
        }

        public static class DeleteRevisionPast {
            public static final String itemName = "DeleteRevisionPast";

            public static final String noteId = "noteId";
            public static final String date = "date";
        }
    }

    public static class TypeElement {

        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String typeId = "typeId";
            public static final String position = "position";
            public static final String title = "title";
            public static final String isArchived = "isArchived";
            public static final String sides = "sides";
            public static final String pattern = "pattern";
            public static final String initialCopy = "initialCopy";
            public static final String data1 = "data1";
            public static final String data2 = "data2";
            public static final String data3 = "data3";
            public static final String data4 = "data4";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String typeId = "typeId";
            public static final String position = "position";
            public static final String title = "title";
            public static final String isArchived = "isArchived";
            public static final String sides = "sides";
            public static final String pattern = "pattern";
            public static final String initialCopy = "initialCopy";
            public static final String data1 = "data1";
            public static final String data2 = "data2";
            public static final String data3 = "data3";
            public static final String data4 = "data4";
        }

        public static class UpdatePosition {
            public static final String itemName = "UpdatePosition";

            public static final String id = "id";
            public static final String position = "position";
        }

        public static class UpdateArchived {
            public static final String itemName = "UpdateArchived";

            public static final String id = "id";
            public static final String isArchived = "isArchived";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }
    }

    public static class Type {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String title = "title";
            public static final String color = "color";
            public static final String position = "position";
            public static final String mode = "mode";
            public static final String isArchived = "isArchived";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String title = "title";
            public static final String color = "color";
            public static final String position = "position";
            public static final String mode = "mode";
            public static final String isArchived = "isArchived";
        }

        public static class UpdatePosition {
            public static final String itemName = "UpdatePosition";

            public static final String id = "id";
            public static final String position = "position";
        }

        public static class UpdateArchived {
            public static final String itemName = "UpdateArchived";

            public static final String id = "id";
            public static final String isArchived = "isArchived";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }
    }

    public static class Profile {
        public static class Add {
            public static final String itemName = "Add";

            public static final String id = "id";
            public static final String position = "position";
            public static final String color = "color";
            public static final String name = "name";
            public static final String isArchived = "isArchived";
            public static final String offline = "offline";
            public static final String imageQualityPercentage = "imageQualityPercentage";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String id = "id";
            public static final String position = "position";
            public static final String color = "color";
            public static final String name = "name";
            public static final String isArchived = "isArchived";
            public static final String offline = "offline";
            public static final String imageQualityPercentage = "imageQualityPercentage";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String id = "id";
        }
    }

    public static class Existence {
        public static class Add {
            public static final String itemName = "Add";

            public static final String pattern = "pattern";
            public static final String type = "type";
            public static final String existenceFlags = "existenceFlags";
            public static final String profile = "profile";
            public static final String state = "state";
            public static final String data1 = "data1";
        }

        public static class Update {
            public static final String itemName = "Update";

            public static final String pattern = "pattern";
            public static final String type = "type";
            public static final String existenceFlags = "existenceFlags";
            public static final String profile = "profile";
            public static final String state = "state";
            public static final String data1 = "data1";
        }

        public static class Delete {
            public static final String itemName = "Delete";

            public static final String pattern = "pattern";
        }

        public static class ClearExistenceFlag {
            public static final String itemName = "ClearExistenceFlag";

            public static final String flag = "flag";
        }
    }
}