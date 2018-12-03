# Spaced Notes
An Android application for memorizing study materials more efficiently using spaced-repetition techninques

<p>This application provides unique features that sets it aport from regular note taking counterparts, including:</p>
<ul>
<li>Creating generic styles to use in multiple notes.</li>
<li>Creating a fully customizable revision pattern for notes.</li>
<li>Creating completely seperate profiles for different study materials.</li>
<li>Syncronizing application data on Dropbox or Google Drive free of charge.</li>
</ul>

# The idea behind Spaced Notes
<p>When studying and learning new concepts, for example mathematical concepts, it is important to repeat the materials in order to retain the information and recall them at a later time. This is one of the reasons why children learn their first language so fast, the words and structures get repeated for them every day. A technique call "spaced repetition" tries to take advantage of this property of our brain to stimulate learning and minimize the chance of forgetting the newly learned concepts. In the spaced repetition technique, after learning a new concept, it gets repeated at first in short intervals and gradually the intervals widen. For instance, a concept can be repeated at days 1, 2, 4, 8, 16, and 32 after learning it for the first times. There is evidence that repeating a concept using spaced repetition in multiple study sessions is more efficient than studying the same material, for the same number of times, in one day.</p>
<p>Spaced Notes provides the tools for taking notes after studying new materials, and offers flexible machanisms to review the notes based on the schedules defined by the user. The way this works is, the application lists the notes that need reviewing in a section called "timeline" and after each review of a note by the user, the application sets the next review date using the schedule that is assigned to that note. The user herself also has the ability to manually change the date for the next review.</p>

# Main elements of Spaced Notes
![Main elements diagram](SpacedNotes/docs/54E92417-67CA-44EE-B2FF-660AAF7D8A37.jpeg)

**Type:**
<p>Users can define one or more types. each type is like a blueprint from which notes can be created. Conversely, each note must be made from one and only one type. A Type indicates the kind of elements and the style of each element in the notes that are created from it. Currently, elements supported for types are texts, text lists, photoes, and section dividers. A type might consist of any number of any of these elements.</p>
![GUI for types](SpacedNotes/docs/3db958c153e244bba9bef4427eb667c3.jpeg)

**Note:**
<p>The materials that users wish to revise are written/inserted in notes. A note itself is made of a number of elements. each element in a note is created using one and only one element from the corresponding type. In other words, the elements of a type, act like blueprints for elements in the notes created from that type. For instance, if a type has two text elements and one photo element, a note made from that type can have any number of elements where each element in the note must be from one of the the three elements of that type. A note also supports ordering/arranging of its elements.</p>

**Schedule:**
<p>The materials that users wish to revise are written/inserted in notes. A note itself is made of a number of elements. each element in a note is created using one and only one element from the corresponding type. In other words, the elements of a type, act like blueprints for elements in the notes created from that type. For instance, if a type has two text elements and one photo element, a note made from that type can have any number of elements where each element in the note must be from one of the the three elements of that type. A note also supports ordering/arranging of its elements.</p>

# Concepts for Developer
todo

