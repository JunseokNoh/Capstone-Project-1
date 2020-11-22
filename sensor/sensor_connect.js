
const format = require('../format_check');

const {pool} = require('../secret_info/db_connect');

//센서 중복 체크 (센서 등록시)
module.exports.sensor_duplication_check = (req,res)=>{
  let wifi_mac_address = req.body.wifi_mac_address; //맥주소 전달 받음
  
  if(!format.checkValidMacAddress(wifi_mac_address))
  {
      res.send({'key':1}) //센서 양식 에러
      return;
  }
  pool.getConnection().then((conn)=>{
    conn.query(`select * from Sensor where wifi_mac_address='${wifi_mac_address}'`).then((data)=>{
      if(data[0]===undefined)
      {
        res.send({'key':2}) //센서 중복 없음 
      }
      else
      {
        res.send({'key':3})//센서 중복 있음
      }
    }).catch((err)=>{
      console.log('센서중복체크시 에러1'+err.code);
      res.send({'key':0,'err_code':err.code});
    })
    conn.release();
  }).catch((err)=>{
    console.log('센서중복체크시 에러2'+err.code);
    res.send({'key':0,'err_code':err.code});
  })
}

module.exports.sensor_registration = (req,res)=>{
  let wifi_mac_address = req.body.wifi_mac_address; //센서 맥주소
  let email_address = req.body.email_address; //사용자 이메일 주소
  let board_nickname = req.body.board_nickname;  //센서 닉네임
  let phone_number = req.body.phone_number; //센서사용자 연락처
  let address = req.body.address; //센서 사용자 주소
  let latitude = req.body.latitude; //센서 위도 
  let longitude = req.body.longitude; //센서 경도 
  if(!format.checkValidMacAddress(wifi_mac_address))
  {
      res.send({'key':1}) //센서 양식 에러
      return;
  }
  if(!format.e_mail_check(email_address))
  {
    res.send({'key':-1}) //이메일 양식 에러
    return;
  }
  if(!format.phone_check(phone_number))
  {
    res.send({'key':-2}) //폰번호양식에러
    return;
  }
  pool.getConnection().then((conn)=>{
    conn.query(`insert into Sensor (wifi_mac_address, board_nickname, phone_number, address, sensor_status, email_address,latitude,longitude)
    values ('${wifi_mac_address}', '${board_nickname}', '${phone_number}', '${address}', '1', '${email_address}','${latitude}','${longitude}')`).then((data)=>{
      console.log('센서등록성공')
      res.send({'key':2})//센서등록성공

    }).catch((err)=>{
      console.log('센서 등록시 에러 발생1 '+err);
      res.send({'key':0,'err_code':err.code});
    })
    conn.release();
  }).catch((err)=>{
    console.log('센서 등록시 에러 발생2 '+err.code);
    res.send({'key':0,'err_code':err.code});
  })
  
}
// 
// function sensor_connect(mac_addr, info, res){
//     pool.getConnection(function(error, connection){
//         let update_query = 'update Sensor set email_address=?, phone_number=?, board_nickname=?, address=?,sensor_status=? where wifi_mac_address=?';  // sensor table에 update
//         connection.query(update_query, [info["email_address"], info["phone_number"], info["board_nickname"],info["address"], '1', mac_addr], function(error,result){
//           if(error){
//             console.log(error);
//             res.status(200).send({'key':error.code});
//           }
//           else{ // 성공적으로 sensor가 연결됨
//               res.send({'key':1});
//           }
//         })
//         connection.release();
//     })
// }

// // sensor가 연결이 안되어 있는지 확인한 후 연결시키는 모듈
// module.exports.check_and_sensor_connect =  function(req,res){
//     let mac_addr = req.body.m_addr; // post로 mac address 받아오기
//     let info = req.body.info; // post로 email, location, board_nickname 받아오기

//     if(format.checkValidMacAddress(mac_addr)){ // 올바른 mac address인가
//       pool.getConnection(function(err, connection){
//         let check_query = 'select sensor_status from Sensor where Sensor.wifi_mac_address=?';
//         connection.query(check_query, mac_addr, function(error, results){
//             if(error){
//               console.log(error);
//               res.status(200).send({'key':-3, 'err_code':error.code});
//               connection.release();
//             }
//             else{
//                 if(results[0]==undefined){ // 해당 sensor가 없을 때
//                   res.send({'key':-1}); // connect 불가능! (sensor 등록이 안됐음)
//                 }
//                 else if(results[0].sensor_status=='0'){
//                     console.log('0입니다');
//                     sensor_connect(mac_addr, info, res);
//                 }
//                 else{
//                     console.log('1입니다');
//                     res.send({'key':-2}); // connect 불가능! (이미 connect 되어있음)
//                 }
//             }
//         })
//       })
//     }
//     else{
//       res.send({'key':0}); // connect 불가능! (mac address 형식 맞지 않음)
//     }
// }