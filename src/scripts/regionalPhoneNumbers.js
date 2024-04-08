// Список добавочных в регионе
var numbersTimezoneMap = {
        'test':{
            numbers: ['6327', '9430', '1435'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'22:00'}
            }
        },
        '9-18':{
            numbers: ['6179', '6171', '3090', '5023', '5022', '5021', '5180', '5172', '5176', '6300', '6306', '6309', '3081', '5504', '5625', '6820', '6821', '6823', '6420', '6416'], 
            schedule:{
                'будние':{startTime:'9:00', endTime:'18:00'}
            }
        },
        '10-19':{
            numbers: ['5235', '5232', '5870', '6158', '6159', '5355', '5350', '5380', '3087', '5629', '6110', '6690', '5968', '6409', '6400', '5999', '5260', '5267', '5009', '5763', '6350', '5951', '5035', '5032', '3804', '5425', '5427', '6480', '5530', '3858', '5858', '5540', '5539', '5780', '5792', '5791', '3809', '1422', '1364', '5100', '5104', '5059', '5693', '6610', '5947', '6660', '6661', '3070', '5729', '5726', '5660', '5762', '5952', '6124', '6122', '5459', '5452', '5453', '3828', '5245', '5246'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'19:00'}
            }
        },
        '93-183':{
            numbers: ['3860', '5080'], 
            schedule:{
                'будние':{startTime:'9:30', endTime:'18:30'}
            }
        },
        '8-17':{
            numbers: ['5273', '5470', '5474', '6683', '6682', '5019', '5012', '5045', '1668'], 
            schedule:{
                'будние':{startTime:'8:00', endTime:'17:00'}
            }
        },
        '8-18':{
            numbers: ['5990', '5993'], 
            schedule:{
                'будние':{startTime:'8:00', endTime:'18:00'}
            }
        },
        '10-18':{
            numbers: ['5633', '5626'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'18:00'}
            }
        },
        '7-16':{
            numbers: ['6380', '6381', '5604', '8238', '8237'], 
            schedule:{
                'будние':{startTime:'7:00', endTime:'16:00'}
            }
        },
        '63-1445':{
            numbers: ['5800', '6181'], 
            schedule:{
                'будние':{startTime:'6:30', endTime:'14:45'}
            }
        },
        '6-15':{
            numbers: ['6870', '6872', '5513', '6867', '6855'], 
            schedule:{
                'будние':{startTime:'6:00', endTime:'15:00'}
            }
        },
        '5-15':{
            numbers: ['6143', '6148'], 
            schedule:{
                'будние':{startTime:'5:00', endTime:'15:00'}
            }
        },
        '4-13':{
            numbers: ['5735', '5733', '5731'], 
            schedule:{
                'будние':{startTime:'4:00', endTime:'13:00'}
            }
        },
        '3-12':{
            numbers: ['6910', '5601', '6800', '6806'], 
            schedule:{
                'будние':{startTime:'3:00', endTime:'12:00'}
            }
        },
        '1-11':{
            numbers: ['6900', '1466'], 
            schedule:{
                'будние':{startTime:'1:00', endTime:'11:00'}
            }
        },
        '9-1910-14':{
            numbers: ['5139', '5138', '5110', '5143', '5962'], 
            schedule:{
                'будние':{startTime:'9:00', endTime:'19:00'},
                'суббота':{startTime:'10:00', endTime:'14:00'}           
            }
        },
        '9-1910-17':{
            numbers: ['6490', '5619', '6492'], 
            schedule:{
                'будние':{startTime:'9:00', endTime:'19:00'},
                'суббота':{startTime:'10:00', endTime:'17:00'}
            }
        },
        '9-2010-16':{
            numbers: ['6440', '5621', '5887'], 
            schedule:{
                'будние':{startTime:'9:00', endTime:'12:00'},
                'суббота':{startTime:'10:00', endTime:'16:00'}
            }
        },
        '10-1910-16':{
            numbers: ['5193', '5191', '5940'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'19:00'},
                'суббота':{startTime:'10:00', endTime:'16:00'}
            }
        },
        '93-2111-16':{
            numbers: ['5830', '5562', '6154', '5564'], 
            schedule:{
                'будние':{startTime:'9:30', endTime:'21:00'},
                'суббота':{startTime:'11:00', endTime:'16:00'}
            }
        },
        '93-2110-16':{
            numbers: ['3077', '1000'], 
            schedule:{
                'будние':{startTime:'9:30', endTime:'21:00'},
                'суббота':{startTime:'10:00', endTime:'16:00'}
            }
        },
        '93-2110-17':{
            numbers: ['3079', '3083', '3084'], 
            schedule:{
                'будние':{startTime:'9:30', endTime:'21:00'},
                'суббота':{startTime:'10:00', endTime:'17:00'}
            }
        },
        '9-1911-16':{
            numbers: ['5340', '5344', '5333'], 
            schedule:{
                'будние':{startTime:'9:00', endTime:'19:00'},
                'суббота':{startTime:'11:00', endTime:'16:00'}
            }
        },
        '10-2010-15':{
            numbers: ['3080', '5819', '5817'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'20:00'},
                'суббота':{startTime:'10:00', endTime:'15:00'}
            }
        },
        '9-191-14':{
            numbers: ['6390'], 
            schedule:{
                'будние':{startTime:'9:00', endTime:'19:00'},
                'суббота':{startTime:'10:00', endTime:'14:00'}
            }
        },
        '10-2011-18':{
            numbers: ['6780', '5061', '5588'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'20:00'},
                'суббота':{startTime:'11:00', endTime:'18:00'}
            }
        },
        '8-189-14':{
            numbers: ['5326', '5301', '5310', '5306'], 
            schedule:{
                'будние':{startTime:'8:00', endTime:'18:00'},
                'суббота':{startTime:'9:00', endTime:'14:00'}
            }
        },
        '8-198-14':{
            numbers: ['5555', '6836'], 
            schedule:{
                'будние':{startTime:'8:00', endTime:'19:00'},
                'суббота':{startTime:'8:00', endTime:'14:00'}
            }
        },
        '7-188-14':{
            numbers: ['5760'], 
            schedule:{
                'будние':{startTime:'7:00', endTime:'18:00'},
                'суббота':{startTime:'8:00', endTime:'14:00'}
            }
        },
        '7-198-14':{
            numbers: ['3082'], 
            schedule:{
                'будние':{startTime:'7:00', endTime:'19:00'},
                'суббота':{startTime:'8:00', endTime:'14:00'}
            }
        },
        '10-191-16':{
            numbers: ['5923', '5917'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'19:00'},
                'суббота':{startTime:'10:00', endTime:'16:00'}
            }
        },
        '10-1912-18':{
            numbers: ['5490', '5493'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'19:00'},
                'суббота':{startTime:'12:00', endTime:'18:00'}
            }
        },
        '102010-17':{
            numbers: ['3870', '6333'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'20:00'},
                'суббота':{startTime:'10:00', endTime:'17:00'}
            }
        },
        '102210-17':{
            numbers: ['5680'], 
            schedule:{
                'будние':{startTime:'10:00', endTime:'22:00'},
                'суббота':{startTime:'10:00', endTime:'17:00'}
            }
        },
    }
// Функция проверяет рабочее время в регионе    
function getOperatorFromNumberAndCurrentTime(additionalNumber){
    
    var schedule = findSchedule(additionalNumber);
    var time = $jsapi.timeForZone("Europe/Moscow");
    var date = currentDate();
    
    var hours = +moment(time).format("H");
    var minutes = +moment(time).format("m");
    var day = date.locale("ru").format("dddd");  
    
    if(!schedule[dayToDatType(day)]){
        return'1000';
    } 
    
    var hoursStart = +moment(schedule[dayToDatType(day)].startTime,'H:mm').format('H');
    var minutesStart = +moment(schedule[dayToDatType(day)].startTime,'H:mm').format('mm');
    var hoursEnd = +moment(schedule[dayToDatType(day)].endTime,'H:mm').format('H');
    var minutesEnd = +moment(schedule[dayToDatType(day)].endTime,'H:mm').format('mm');
    
    if((hours < hoursStart) || (hours > hoursEnd)){
        return'1000';
    } else if(hours == hoursStart && minutes < minutesStart) {
        
        return'1000';
    } else if(hours == hoursEnd && minutes > minutesEnd) {
         
        return'1000';
    }
    return additionalNumber;
}
 // Функция проверяет есть ли указанный клиентом добавочный в перечне региональных
function findSchedule(additionalNumber){
    for (var x in numbersTimezoneMap) {
        for (var number in numbersTimezoneMap[x].numbers){
            if(additionalNumber == numbersTimezoneMap[x].numbers[number]){
                return numbersTimezoneMap[x].schedule;
            }
        }
    }
}
// Функция определяет деннь выходной или рабочий (привязки к дате нет)
function dayToDatType(day){
    switch (day){
        case 'суббота':
        case 'воскресенье' :
            return day;
        default:
            return 'будние';
    }
}