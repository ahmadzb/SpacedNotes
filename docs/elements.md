# Main elements of Spaced Notes
![Main elements diagram](SpacedNotes/docs/54E92417-67CA-44EE-B2FF-660AAF7D8A37.jpeg)

**Type:**
<p>Users can define one or more "types". each type is like a blueprint from which notes can be created. Conversely, each note must be made from one and only one type. A Type indicates the kind of elements and the style of each element in the notes that are created from it. Currently, elements supported for types are texts, text lists, photoes, and section dividers. A type might consist of any number of any of these elements.</p>

![GUI for types](SpacedNotes/docs/3db958c153e244bba9bef4427eb667c3.jpeg)


**Note:**
<p>The materials that users wish to revise are written/inserted in notes. A note itself is made of a number of elements. each element in a note is created using one and only one element from the corresponding type. In other words, the elements of a type, act like blueprints for elements in the notes created from that type. For instance, if a type has two text elements and one photo element, a note made from that type can have any number of elements where each element in the note must be from one of the the three elements of that type. A note also supports ordering/arranging of its elements.</p>

![GUI for notes](SpacedNotes/docs/9e69c9dcf138459fb1a9e7ee71963919.jpeg)


**Schedule:**
<p>The user defines one ore more "schedules" to specify the way the notes should be revised. Each schedule includes a set of ordered numbers indicating the minimum number of days between revisions. The machanism of the schedules is best explained by an example. Lets say we have a schedule with numbers 1, 2, 4, 8, 16, and 32 in that order. Once a note is assigned to that schedule, after 1 day of the assignment date, the note will show up for revision. Whenever the user marks the note as revised, the note will show up again after 2 days of that revision date. This means that if the user revise the note after 2 days instead of after 1 day, the note will show up again after 2+2=4 days of the initial assignment to the schedule and not 1+2=3 days. This cycle will repeat for all elements in the sequence of the schedule.</p>

![GUI for schedules](SpacedNotes/docs/7f35482684f84a3985613b8e5984d07a.jpeg)


**Timeline:**
<p>Timeline is the main page of the application where the user can see what notes need revision, when and what notes had been revised previously, when and what notes should be revised in the future. Timeline hosts one section for each day, hence three kinds of sections should exist. one section for current day, sections for past days, sections for future days. The section for current day presents all the notes that require revision, whether the revision was due days ago or the current day; it also shows the revisions done in the current day. The sections for past days retain the history of previously done revisions and the sections for future days view the notes that should be revised in the specified day.</p>

# Profiles, Labels, and Label lists

**Profiles:**
<p>Profiles enable the user to seperate content for different topics. Each profile is completely seperate from other profiles, meaning that there is no data sharing between profiles. A profile has its own notes, types, schedules, revisions, labels, and basically every other entity in the app. Even the physical database for each profile is seperate. Profiles are ideal for seperating study materials for unrelated topics, such as mathematics and English language.</p>

![GUI for profiles, labels in a label lists, and main menu](SpacedNotes/docs/55474318cdb94125a07e61198044b42c.jpeg)

**Labels:**
<p>Each note can associate with a number of labels. Using labels, it becomes possible to view notes with similar features by labeling them with a specific label. Labels are exactly as their names suggest; they label the notes so it is possible to find the note later using that label.</p>

**Label List:**
<p>Label lists are entities that include an ordered list of either labels or label lists. Label lists make it possible to create a tree-like hierarchy of labels. It can be used, for instance, to simulate sections and chapters of a book.</p>

# Syncing
<p>Syncing enables the user to use the appliction accross multiple devices. It also stores the data safely in the cloud to prevent data loss. Since this application uses google drive or dropbox to save data online, it is both free and provides a high level of data privacy. The structure of the user data is designed in a way (more explanation in developer section) that by each time syncing, only the new changes are uploaded and hence syncing is both fast and requires minimum data transfer over the internet.</p>
