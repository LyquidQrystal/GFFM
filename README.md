# Overview 
A simple minecraft mod that allows you to use instrument from Immersive Melodies to play music to your pokemon(from the Cobblemon mod) and increase the friendship.
# Features
* Check the [Modrinth page](https://modrinth.com/mod/gain-friendship-from-melodies) for the full feature list.
## How to use the config to add instrument sound to Pokemon?
The rules are formatted as follows:
INSTRUMENT|SPECIES_NAME|REGIONAL_FORM|GENDER|NATURE|ABILITY|MOVE|FRIENDSHIP
Please keep in mind that this is case-sensitive.(This is changed since 0.2.2.)
### Definition
* INSTRUMENT  
It's the instrument sound you want the Pokemon to use. I didn't add any new instrument so you can only use bagpipe,didgeridoo,flute,lute,piano,triangle,trumpet and tiny_drum
* SPECIES_NAME  
It's the species name of the Pokemon(Rillaboom,Incineroar,Urshifu,Pikachu,etc.). Use "any" if you don't want to apply this rule to a specific species.
* REGIONAL_FORM  
The regional form of the Pokemon(Alola,Hisui,etc.). Use "any" if you don't want to apply this rule to a specific regional form.
* GENDER  
The gender of the Pokemon(FEMALE,MALE,GENDERLESS). Use "any" if you don't want to apply this rule to a specific gender.
* NATURE  
The nature of the Pokemon(adamant,modest,jolly,timid,etc.). Use "any" if you don't want to apply this rule to a specific nature.
* ABILITY  
The ability of the Pokemon(intimidate,liquidvoice,etc.). Use "any" if you don't want to apply this rule to a specific ability.
* MOVE  
The move your Pokemon needs to **learn** (bellydrum,grasswhistle,etc.). Use "any" if you don't want to add a move restriction.
* FRIENDSHIP  
The **minimum** friendship required to start imitate.It should be an integer,but you can also use "any" if you don't want to apply this rule to a specific ability.
### Example
lute|Raichu|Alola|FEMALE|timid|surgesurfer|thunderbolt|100  
After adding this rule,if a timid,female Raichu learns Thunderbolt and the friendship value reaches 100, it will use the sound of the lute to imitate your performance.(Yeah,that's not logical.It's just an example.^_^)
# Known issue
None yet.
# Download
* [Curseforge]()
* [Modrinth](https://modrinth.com/mod/gain-friendship-from-melodies)