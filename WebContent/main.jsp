<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<script
  src="https://code.jquery.com/jquery-3.2.1.min.js"
  integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4="
  crossorigin="anonymous"></script>
<title>Visualizador de rede DHT</title>
</head>
<body>
	<div align="center">
		<div id="peersContainer" style="width: 80%" align="center"></div>
		<div style="width: 80%" align="right">
			Última atualização: <span id="updatedAt"></span>
		</div>
	</div>
	<script>
		function getPeers() {
			$.ajax({
				method: "POST",
				dataType: "json",
				success: function(peers) {
					console.log(peers)
					$('#peersContainer').empty()
					peers.forEach(function(peer) {
						var $table = $('<table></table>')
						$table.addClass('table')
						$table.addClass('table-striped')
						$table.addClass('peers-table')

						var $header = $('<thead></thead>')
						var $th = $('<tr></tr>').addClass('bg-primary').appendTo($header)
						$("<td></td>").attr("colspan", "4").html(peer.id).appendTo($th)

						var $body = $("<tbody></tbody>")
						var $tr = $("<tr></tr>").appendTo($body)
						
						
						var signKeyContents = peer.signPublicKey ? "<a href='#' class='download' data-name='sign_key.txt' data-content='" + peer.signPublicKey + "'><span class='glyphicon glyphicon-lock'></span></a>" : "null";
						var chatKeyContents = peer.signPublicKey ? "<a href='#' class='download' data-name='pgp_key.txt' data-content='" + peer.chatPublicKey + "'><span class='glyphicon glyphicon-lock'></span></a>" : "null";
						$("<td></td>").css('width','25%').html(peer.name + "<br>" + peer.address).appendTo($tr)
						$("<td></td>").css('width','25%').attr('align', 'center').html("Chave DSA Pública<br>" + signKeyContents).appendTo($tr)
						$("<td></td>").css('width','25%').attr('align', 'center').html("Chave PGP Pública<br>" + chatKeyContents).appendTo($tr)
						$("<td></td>").css('width','25%').html("Endereço Chat<br>" + peer.chatAddress).appendTo($tr)

						$table.append($header)
						$table.append($body)
						$('#peersContainer').append($table)
					})
					$('#updatedAt').html(new Date())
				},
				error: function(err) {
					console.error(err)
				}
			})
		}
		$(function() {
			getPeers();
			setInterval(getPeers, 5000);
		})
		
		$(document).on('click', '.download', function(e) {
			e.preventDefault();
			var content = $(this).attr('data-content');
			var filename = $(this).attr('data-name');
			var bytes = atob(content);
			var fileBlob = new Blob([bytes], {type: "application/octet-binary"});
			var link = document.createElement("a");
			link.setAttribute("href", URL.createObjectURL(fileBlob));
			link.setAttribute("download", filename);
			link.click();
		})
	</script>
</body>
</html>