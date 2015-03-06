# HoloHead
<strong>config.yml</strong>
apiUrl: http://my.epic.host/holoApi
broadcast:
- { JSON holo page 1}
- { JSON holo page 2}

<strong>API:</strong>
Number of player based holos:
Request: get aipUrl + "?playerHoloCount"
Result: {Count: X}

Number of global holos:
Request: get apiUrl + "?globalHoloCount"
Result: {Count: X}

Get player based holo:
Request: get apiUrl + "?playerHolo&ID=X&PlayerName=PlayerName"
Result: See result

Get global holo:
Request: get apiUrl + "?globalHolo&ID=X"
Result: See result

Holo result:
{Type: [Global,Player], Title: [HoloTitle], Lines: [Lines], Player: [Player], StayTIme: [XSeconds], UpdateIn: [XSeconds], Error: [ErrorMessage], HoloId: [HologramId], SortId: [SortNumber]}

<strong>LINK TO HOLOGRAPHIC DISPLAYS</strong>: http://dev.bukkit.org/bukkit-plugins/holographic-displays/
