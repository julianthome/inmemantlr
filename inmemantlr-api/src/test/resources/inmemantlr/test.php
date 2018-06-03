<?php


    $config["link"] = "blue";
    $config["delbut"] = "http://localhost/delbut.png";
    $config["showip"] = "1";
    $config["ipbut"] = "http://localhost/ipbut.png";
    $config["emailbut"] = "http://localhost/emailbut.png";
    $config["homebut"] = "http://localhost/homebut.png";
    $thisprogram = "pblguestbook.php";
    $mes["email"] = "email";
    $mes["website"] = "website URL";
    $mes["delete"] = "delete";


    if ($_SERVER['REMOTE_ADDR'] != '' && $config['showip'] == '1') {
        $ipbut = "&nbsp;<IMG SRC=$config[ipbut] BORDER=0 ALT=\"";
        $ipbut .= $_SERVER["REMOTE_ADDR"] . "\">&nbsp;";
    }

    $delbut = "&nbsp;<A STYLE=COLOR:$config[link]; HREF=$thisprogram?action=delete&id=$_POST[id]><IMG SRC=$config[delbut] BORDER=0 ALT=\""; 
    $delbut .= $mes['delete'] . "\"></A>&nbsp;";

    if ($_POST['website'] != '') {
        $_POST["website"] = preg_replace("/\<SCRIPT(.*?)\>(.*?)\<\/SCRIPT(.*?)\>/i", "SCRIPT BLOCKED", $_POST["website"]);
        $homebut = "&nbsp;<A STYLE=COLOR:$config[link]; HREF=\"http://$_POST[website]\"><IMG SRC=$config[homebut] BORDER=0 ALT=\"";
        $homebut .= $mes['website'] . "\"></A>&nbsp;";
    }

    if ($_POST['email'] != '') {
        $_POST["email"] = preg_replace("/\<SCRIPT(.*?)\>(.*?)\<\/SCRIPT(.*?)\>/i", "SCRIPT BLOCKED", $_POST["email"]);
        $emailbut = "&nbsp;<A STYLE=COLOR:$config[link]; HREF=\"mailto:$_POST[email]\"><IMG SRC=$config[emailbut] BORDER=0 ALT=\"";
        $emailbut .= $mes['email'] . "\"></A>&nbsp;";
    }
    $pdata .= "</TD><TD STYLE=TEXT-ALIGN:right;><FONT SIZE=1>$emailbut$homebut$ipbut$delbut</FONT></TD></TR>";

    echo $pdata;

?>
    
