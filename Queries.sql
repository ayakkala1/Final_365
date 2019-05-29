use nstapp;

-- R1

# May want to check edge case of collisions in future
SELECT * FROM
(
	SELECT DISTINCT recent.*
	FROM
		(
		SELECT Room,RoomName,Checkin as lastCheckIn,Checkout as lastCheckOut, recentLength,
							(SELECT 
                            (CASE
								WHEN cases = 1 THEN cor1.Checkout
                                ELSE CURDATE()
							END
                            )
                            FROM
								(
                                SELECT v1.Room,Checkin,Checkout,RANK() over (ORDER BY DATEDIFF(Checkout,CURDATE()))  ranks,
											(
                                            CASE
                                             WHEN  DATEDIFF(Checkin,CURDATE()) > 0  and CURDATE() < (SELECT MIN(Checkin) FROM lab7_reservations
																																					WHERE
																																					lab7_reservations.CheckOut >= CURDATE()
																																					and lab7_reservations.Room = t.Room 
																																				 ) THEN 0
											ELSE 1
                                            END
                                            ) cases
									FROM lab7_reservations v1
										INNER JOIN lab7_rooms r1 on (v1.Room = r1.RoomCode)
									WHERE 
											(
                                            v1.CheckOut >= CURDATE()
											and v1.Room = t.Room 
											and  r1.RoomName = t.RoomName 
											and v1.Checkout NOT IN 
															(
															SELECT Checkin FROM lab7_reservations
															WHERE lab7_reservations.Room = v1.Room and lab7_reservations.CODE <> v1.CODE
															)
											) 
								) cor1
							WHERE ranks = 1
							)	nextAvailability
			FROM (
				SELECT *, 
					DATEDIFF(Checkout,CheckIn) recentLength ,
					DATEDIFF(curdate(),CheckIn),
					RANK() over (PARTITION by Room ORDER BY DATEDIFF(curdate(),CheckIn)) ranks 
				FROM lab7_reservations v
					INNER JOIN lab7_rooms r on (v.Room = r.RoomCode)
					WHERE DATEDIFF(curdate(),CheckIn) >= 0	
						) AS t
		WHERE ranks = 1
		) AS recent
) AS r23
NATURAL JOIN
(
	SELECT Room,RoomName,
		ROUND(
			SUM(
				CASE 
					WHEN (Checkout < date_sub(CURDATE(), INTERVAL 180 DAY) or (Checkin > CURDATE())) THEN 0
					WHEN CheckIn < CURDATE() and Checkout > CURDATE() THEN DATEDIFF(CURDATE(),Checkin)
					WHEN Checkin < date_sub(CURDATE(), INTERVAL 180 DAY) and Checkout > date_sub(curdate(), INTERVAL 180 DAY) THEN DATEDIFF(Checkout,CURDATE())
					ELSE DATEDIFF(Checkout,Checkin)
				END 
				   )/ 180 
			        , 2) AS proportion_last_180_days
		FROM lab7_reservations v
			INNER JOIN lab7_rooms r on (v.Room = r.RoomCode)
		GROUP BY Room,RoomName
) AS r1 
