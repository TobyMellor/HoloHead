# HoloHead
<strong>config.yml</strong><br />
apiUrl: http://my.epic.host/holoApi<br />
broadcast:<br />
- { JSON holo page 1}<br />
- { JSON holo page 2}<br />
<br />
<strong>API:</strong><br />
Number of player based holos:<br />
Request: get aipUrl + "?playerHoloCount"<br />
Result: {Count: X}<br />
<br />
Number of global holos:<br />
Request: get apiUrl + "?globalHoloCount"<br />
Result: {Count: X}<br />
<br />
Get player based holo:<br />
Request: get apiUrl + "?playerHolo&ID=X&PlayerName=PlayerName"<br />
Result: See result<br />
<br />
Get global holo:<br />
Request: get apiUrl + "?globalHolo&ID=X"<br />
Result: See result<br />
<br />
Holo result:<br />
{Type: [Global,Player], Title: [HoloTitle], Lines: [Lines], Player: [Player], StayTIme: [XSeconds], UpdateIn: [XSeconds], Error: [ErrorMessage], HoloId: [HologramId], SortId: [SortNumber]}<br />
<br />
<strong>LINK TO HOLOGRAPHIC DISPLAYS</strong>: http://dev.bukkit.org/bukkit-plugins/holographic-displays/
