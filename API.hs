--API--

module API where

-----------
--IMPORTS--
-----------

import qualified Data.HashMap.Strict as HM
import qualified Data.List.Split as SP
import qualified Data.List as IS

--------------------
--DATA STRUCTURES --
--------------------

	-------------------
	--REGULAR GRAMMAR--
	-------------------

type SYMBOLS = [String]

data GR = GR { 		nonTerminals 	:: SYMBOLS
			   ,	terminals 		:: SYMBOLS
			   ,	predicates 		:: HM.HashMap String [String]
			   ,	initialState 	:: String
			 } deriving (Show)

		------------
		--CREATION--
		------------

--Creates, if possible, a regular grammar.
createGR :: SYMBOLS -> SYMBOLS -> HM.HashMap String [String] -> String -> Maybe GR
createGR nt t p is 
	|and [(disjoint nt t), (belongs nt is /= -1), isRegular (GR nt t p is)] = Just (simplify (GR nt t p is))
	|otherwise = Nothing

		--------------
		--VALIDATION--
		--------------

--Checks if a regular grammar is valid.
isGR :: Maybe GR -> Bool
isGR (Just g) = True
isGR _ = False

--Checks if two lists are disjointed.
disjoint :: Eq a => [a] -> [a] -> Bool
disjoint [] _ = True
disjoint _ [] = True
disjoint (x:xs) l 
	|belongs l x /= -1 = False
	|otherwise = disjoint xs l

evalHM :: Maybe [String] -> [String]
evalHM (Just xs) = xs
evalHM _ = []

--Checks if a grammar is regular.
isRegular :: GR -> Bool
isRegular (GR nt t p _) = isRegular' nt nt t p

isRegular' :: SYMBOLS -> SYMBOLS -> SYMBOLS -> HM.HashMap String [String] -> Bool
isRegular' [] _ _ _ = True
isRegular' (sy:sys) nt t m = (isRegular'' (evalHM (HM.lookup sy m)) nt t) && (isRegular' sys nt t m)

-- isRegular' sys nt t m = and (map (\sy -> isRegular'' (evalHM (HM.lookup sy m)) nt t) sys) 

isRegular'' :: [String] -> SYMBOLS -> SYMBOLS -> Bool
isRegular'' [] _ _ = True
isRegular'' (v:vs) nt t 
	|v=="\\" = isRegular'' vs nt t 
	|otherwise = and [(isRegular''' (SP.splitOn " " v) nt t False False),(isRegular'' vs nt t)]

isRegular''' :: [String] -> SYMBOLS -> SYMBOLS -> Bool -> Bool -> Bool
isRegular''' [] _ _ b1 b2 = or [b1,b2]	 
isRegular''' (c:cs) nt t b1 b2
	|belongs nt c /= -1 = if(b1) then False else (isRegular''' cs nt t True b2)
	|belongs t c /= -1 = if(b2) then False else (isRegular''' cs nt t b1 True)
	|otherwise = False	

--Removes unitary symbols
squash :: GR -> GR
squash (GR nt t p is) = simplify (squash' (GR nt t p is) nt)

squash' ::  GR -> SYMBOLS -> GR
squash' g [] = g
squash' g (nt:nts) = squash' (squash'' (Just g) g nt) nts 

squash'' :: Maybe GR -> GR -> String -> GR
squash'' Nothing g _ = g
squash'' (Just (GR nt t p is)) _ k = squash'' (isNT (evalHM (HM.lookup k p)) k (GR nt t p is)) (GR nt t p is) k 

isNT :: [String] -> String -> GR -> Maybe GR 
isNT [] _ _ = Nothing
isNT (v:vs) k (GR nt t p is) 
	|belongs nt v /= -1 = Just (addPredicate (removePredicate (GR nt t p is) k v) k (evalHM (HM.lookup v p))) 
	|otherwise = isNT vs k (GR nt t p is)

--Detects and removes unreachable and inproductive symbols from a regular grammar.
simplify :: GR -> GR
simplify (GR nt t p is) = byeBye (GR nt t p is) (simplify' (GR nt t p is) ((initProd (GR nt t p is) nt),(is:[])))

simplify' :: GR -> (SYMBOLS, SYMBOLS) -> SYMBOLS
simplify' (GR nt t p is) (prod, reach) = simplifyAux (GR nt t p is) (prod, reach) (simplifyIT (GR nt t p is) Nothing (prod, reach) nt) 
	
simplifyAux :: GR -> (SYMBOLS, SYMBOLS) -> Maybe (SYMBOLS, SYMBOLS) -> SYMBOLS
simplifyAux (GR nt _ _ _) (prod, reach) Nothing = removeAll nt (IS.intersect prod reach)
simplifyAux g _ (Just (prod, reach)) = simplify' g (prod, reach) 

simplifyIT :: GR -> Maybe (SYMBOLS, SYMBOLS) -> (SYMBOLS, SYMBOLS) -> SYMBOLS -> Maybe (SYMBOLS, SYMBOLS)
simplifyIT _ ans _ [] = ans
simplifyIT (GR nt t p is) new old (sy:sys) = simplifyIT (GR nt t p is) (simplifyIT' (GR nt t p is) (evalHM (HM.lookup sy p)) sy new old) old sys 

simplifyIT' :: GR -> [String] -> String -> Maybe (SYMBOLS, SYMBOLS) -> (SYMBOLS, SYMBOLS) -> Maybe (SYMBOLS, SYMBOLS)
simplifyIT' _ [] _ ans _ = ans
simplifyIT' (GR nt t p is) (v:vs) k Nothing (prod, reach)
	|and [(belongs prod k /= -1),(belongs reach k == -1)] = Nothing 
	|otherwise = simplifyIT' (GR nt t p is) vs k (simplifyIT'' k (getNT (SP.splitOn " " v) nt) Nothing (prod, reach)) (prod, reach)
simplifyIT' (GR nt t p is) (v:vs) k (Just (prod, reach)) old
	|and [(belongs prod k /= -1),(belongs reach k == -1)] = Just (prod, reach) 
	|otherwise = simplifyIT' (GR nt t p is) vs k (simplifyIT'' k (getNT (SP.splitOn " " v) nt) (Just (prod, reach)) old) old

simplifyIT'' :: String -> Maybe String -> Maybe (SYMBOLS, SYMBOLS) -> (SYMBOLS, SYMBOLS) -> Maybe (SYMBOLS, SYMBOLS)
simplifyIT'' _ Nothing new _ = new
simplifyIT'' k (Just s) Nothing (prod, reach) 
	|and [(belongs prod k == -1),(belongs prod s /= -1)] = Just ((k:prod),reach)
	|and [(belongs reach k /= -1),(belongs reach s == -1)] = Just (prod,(s:reach))
	|otherwise = Nothing
simplifyIT'' k (Just s) (Just (prod, reach)) _ 
	|and [(belongs prod k == -1),(belongs prod s /= -1)] = Just ((k:prod),reach)
	|and [(belongs reach k /= -1),(belongs reach s == -1)] = Just (prod,(s:reach))
	|otherwise = Just (prod, reach)

getNT :: [String] -> SYMBOLS -> Maybe String
getNT [] _ = Nothing
getNT (c:cs) nt
	|belongs nt c /= -1 = (Just c)
	|otherwise = getNT cs nt

initProd :: GR -> SYMBOLS -> SYMBOLS
initProd _ [] = []
initProd (GR nt t p is) (sy:sys) = (initProd' (GR nt t p is) (evalHM (HM.lookup sy p)) sy) ++ (initProd (GR nt t p is) sys)

initProd' :: GR -> [String] -> String -> [String]
initProd' _ [] _ = []
initProd' (GR nt t p is) (v:vs) k 
	|or [v=="\\" , (belongs t v /= -1)] = k:[]
	|otherwise = initProd' (GR nt t p is) vs k

--Removes unreachable and inproductive symbols from a regular grammar.
byeBye :: GR -> SYMBOLS -> GR
byeBye (GR nt t p is) sys = byeBye' (cleanMap (GR (removeAll nt sys) t p is) sys) sys nt

byeBye' :: GR -> SYMBOLS -> STATES -> GR
byeBye' g _ [] = g
byeBye' (GR nt t p is) sys (st:sts) = byeBye' (byeBye'' (GR nt t p is) sys st (evalHM (HM.lookup st p))) sys sts  

byeBye'' :: GR -> SYMBOLS -> String -> [String] -> GR
byeBye'' g _ _ [] = g
byeBye'' g sys st (v:vs) = byeBye'' (byeBye''' g sys st v (SP.splitOn " " v)) sys st vs

byeBye''' :: GR -> SYMBOLS -> String -> String -> [String] -> GR
byeBye''' g _ _ _ [] = g
byeBye''' g sys st v (c:cs) 
	|belongs sys c == -1 = byeBye''' g sys st v cs 
	|otherwise = removePredicate g st v 

cleanMap :: GR -> SYMBOLS -> GR
cleanMap g [] = g 
cleanMap (GR nt t p is) (sy:sys) = cleanMap (GR nt t (HM.delete sy p) is) sys  

		----------------
		--MODIFICATION--
		----------------

--Adds a predicate, if possible, into a regular grammar.
addPredicate :: GR -> String -> [String] -> GR
addPredicate (GR nt t p is) from to = addPredicate' (GR nt t p is) from to (evalHM (HM.lookup from p)) 

addPredicate' :: GR -> String -> [String] -> [String]-> GR
addPredicate' (GR nt t p is) from to olds = (GR nt t (HM.insert from (olds++(removeAll to (from:olds))) p) is)

--Adds a list of predicates, if possible, into a regular grammar.
addPredicates :: GR -> HM.HashMap String [String] -> STATES-> GR 
addPredicates g _ [] = g 
addPredicates g m (st:sts) = addPredicates (addPredicate g st (evalHM (HM.lookup st m))) m sts

--Removes a predicate from a regular grammar.
removePredicate :: GR -> String -> String -> GR
removePredicate (GR nt t p is) from to 
	|HM.member from p = (GR nt t (HM.insert from (remove (evalHM (HM.lookup from p)) to) p) is) 
	|otherwise = (GR nt t p is)

--Removes a list of elements from a list.
removeAll :: Eq a => [a] -> [a] -> [a]
removeAll [] _ = []
removeAll l [] = l
removeAll l (x:xs) = removeAll (remove l x) xs

--Removes an element, if exists, from a list.
remove :: Eq a => [a] -> a -> [a]
remove [] _ = []
remove (x:xs) e 
	|x==e = xs
	|otherwise = x:remove xs e

		------------------
		--TRANSFORMATION--
		------------------

--Gets a regular grammar and turns it into a right oriented regular grammar.
toRight :: GR -> GR
toRight (GR nt t p is) = squash (toRight' (GR nt t p is) HM.empty nt) 

toRight' :: GR -> HM.HashMap String [String] -> STATES -> GR
toRight' (GR nt t _ is) m [] = (GR ("FF":nt) t (HM.insert is ("\\":(getHM (HM.lookup is m))) m) "FF")   
toRight' (GR nt t p is) m (st:sts) = toRight' (GR nt t p is) (itRight (GR nt t p is) m (getHM (HM.lookup st p)) st) sts

itRight :: GR -> HM.HashMap String [String] -> [String] -> String -> HM.HashMap String [String]
itRight _ m [] _ = m
itRight g m (v:vs) st = itRight g (itRight' g st (SP.splitOn " " v) m v) vs st

itRight' :: GR -> String -> SYMBOLS -> HM.HashMap String [String] -> String -> HM.HashMap String [String]
itRight' (GR nt t p is) st sys m v
	|length sys == 1 = (caseRight (GR nt t p is) st (last sys) m v)   
	|otherwise = if((belongs nt (last sys)) == -1) then (HM.insert (head sys) (((last sys)++" "++st):(getHM (HM.lookup (head sys) m))) m) else (HM.insert st (v:(getHM (HM.lookup st m))) m)

caseRight :: GR -> String -> String -> HM.HashMap String [String] -> String -> HM.HashMap String [String]
caseRight (GR nt t p is) st "\\" m v = (HM.insert "FF" (st:(getHM (HM.lookup "FF" m))) m)
caseRight (GR nt t p is) st sym m v 
	|(belongs nt sym) == -1 = (HM.insert "FF" ((v++" "++st):(getHM (HM.lookup "FF" m))) m)
	|otherwise = (HM.insert st (v:(getHM (HM.lookup st m))) m)

--Converts a regular grammar into an AFNDL.
toAFNDL :: GR -> Maybe AFNDL
toAFNDL g = toAFNDL' (toRight g)

toAFNDL' :: GR -> Maybe AFNDL 
toAFNDL' (GR nt t p is) = fillNDL (createAFNDL t nt (makeFinalStatesL nt p) is) p

fillNDL :: Maybe AFNDL -> HM.HashMap String [String] -> Maybe AFNDL
fillNDL (Just (AFNDL a st fs is d)) m = Just (makeDeltaL (AFNDL a st fs is d) m)
fillNDL Nothing _ = Nothing

--Makes a list of all final states in a regular grammar.
makeFinalStatesL :: STATES -> HM.HashMap String [String] -> STATES
makeFinalStatesL [] _ = []
makeFinalStatesL (st:sts) m
	|belongs (getHM (HM.lookup st m)) "\\" == -1 = makeFinalStatesL sts m
	|otherwise = st:(makeFinalStatesL sts m)

--Makes a delta matrix.
makeDeltaL :: AFNDL -> HM.HashMap String [String] -> AFNDL
makeDeltaL (AFNDL a st fs is d) m = makeDeltaL' (AFNDL a st fs is d) st m

makeDeltaL' :: AFNDL -> STATES -> HM.HashMap String [String] -> AFNDL
makeDeltaL' a [] _ = a
makeDeltaL' a (st:sts) m = makeDeltaL' (convertDelta a st (getHM (HM.lookup st m))) sts m

--Converts a list of productions into a delta matrix.
convertDelta :: AFNDL -> String -> [String] -> AFNDL
convertDelta a _ [] = a
convertDelta a s (p:ps) 
	|p == "\\"  = convertDelta a s ps 
	|otherwise = (convertDelta (convertDelta' a s (SP.splitOn " " p)) s ps) 

convertDelta' :: AFNDL -> String -> [String] -> AFNDL
convertDelta' a s (c:st) = setDeltaNDL a s c ((last st):(getDeltaNDL a s c))

	--Makes a delta matrix.
	--makeDeltaL :: AFNDL -> HM.HashMap String [String] -> DELTASL
	--makeDeltaL (AFNDL a st fs is d) m = makeDeltaL' (AFNDL a st fs is d) st m

	--makeDeltaL' :: AFNDL -> STATES -> HM.HashMap String [String] -> DELTASL
	--makeDeltaL' _ [] _ = []
	--makeDeltaL' a (st:sts) m = (convertDelta a st (getHM (HM.lookup st m))):(makeDeltaL' a sts m) 

	--Converts a list of productions into a delta matrix.
	--convertDelta :: AFNDL -> String -> [String] -> [STATES]
	--convertDelta _ _ [] = []
	--convertDelta a s (p:ps) 
	--	|p == "\\"  = convertDelta a s ps 
	--	|otherwise = (convertDelta (convertDelta' a s (SP.splitOn " " p)) s ps) 

	--convertDelta' :: AFNDL -> String -> [String] -> AFNDL
	--convertDelta' a s (c:st) = setDeltaNDL a s c ((last st):(getDeltaNDL a s c))

	-------------------
	--FINITE AUTOMATA--
	-------------------

type ALPHABET 	= [String]
type STATES 	= [String]
type DELTASL	= [[[String]]]
type DELTASI	= [[Maybe String]]

data AFNDL = AFNDL {	alphabetNDL	 	:: ALPHABET
					,	statesNDL		:: STATES
					,	finalStateNDL 	:: STATES
					,	initialStateNDL	:: String
					,	deltasNDL 		:: DELTASL
			   	   } deriving (Show)


data AFND = AFND {		alphabetND	 	:: ALPHABET
					,	statesND		:: STATES
					,	finalStateND 	:: STATES
					,	initialStateND	:: String
					,	deltasND 		:: DELTASL
			   	 } deriving (Show)


data AFD = 	AFD {		alphabetD 		:: ALPHABET
					,	statesD 		:: STATES
					,	finalStateD 	:: STATES
					,	initialStateD 	:: String
					,	deltasD 		:: DELTASI
			    } deriving (Show)

--Returns, if possible, the position of an element in a list.
belongs :: Eq a => [a] -> a -> Int
belongs [] _ = -1
belongs (x:xs) s = belongs' (x:xs) s 0

belongs' :: Eq a => [a] -> a -> Int -> Int
belongs' [] _ _ = -1
belongs' (x:xs) s n 
	|x==s = n
	|otherwise = belongs' xs s (n+1)

belongsAll :: Eq a => [a] -> [a] -> Bool
belongsAll [] _ = True
belongsAll _ [] = True
belongsAll l (x:xs) 
	|belongs l x == -1 = False
	|otherwise = belongsAll l xs

evalToList :: Maybe String -> [String]
evalToList (Just s) = [s]
evalToList _ = []

getHM :: Maybe [String] -> [String]
getHM (Just s) = s
getHM _ = []

	---------------------------------
	--DETERMINISTIC FINITE AUTOMATA--
	---------------------------------

		------------
		--CREATION--
		------------

--Creates, if possible, an AFD.
createAFD :: ALPHABET -> STATES -> STATES -> String -> Maybe AFD
createAFD a s f i 
	|belongsAll s (i:f) = Just (AFD a s f i (createList (length s) (createList (length a) Nothing)))
	|otherwise = Nothing
		
		--------------
		--VALIDATION--
		--------------

--Checks if an AFD is valid.
isAFD :: Maybe AFD -> Bool
isAFD (Just a) = True
isAFD _ = False

--Checks if the final and initial states belongs to the list of states.
hasValidStates :: AFD -> Bool
hasValidStates (AFD _ st fs is _) 
	|belongs st is == -1 = False
	|otherwise = belongsAll st fs 

--Check if a word belongs to the generated language
isProduced :: AFD -> String -> Bool
isProduced (AFD alp st fs i d) word = isProducedFrom (AFD alp st fs i d) word i  

isProducedFrom :: AFD -> String -> String -> Bool
isProducedFrom (AFD alp sts fs i d) [] st = (belongs fs st /= -1)
isProducedFrom a (c:cs) st = isProducedFrom' a (c:cs) (getDeltaD a st [c])

isProducedFrom' :: AFD -> String -> Maybe String -> Bool
isProducedFrom' (AFD alp sts fs i d) [] (Just next) = (belongs fs next /= -1)
isProducedFrom' (AFD alp sts fs i d) _ Nothing = False
isProducedFrom' a (c:cs) (Just next) = isProducedFrom' a cs (getDeltaD a next [c])
		----------------
		--MODIFICATION--
		----------------
		
--Returns, if possible, the edge from a state with an alphabet character.
getDeltaD :: AFD -> String -> String -> Maybe String
getDeltaD (AFD alp st _ _ d) s c = auxDeltaD (belongs st s) (belongs alp c) d

--Gets, if possible, the edge from the delta matrix.
auxDeltaD :: Int -> Int -> DELTASI -> Maybe String
auxDeltaD (-1) _ _ = Nothing
auxDeltaD _ (-1) _ = Nothing
auxDeltaD i j d = d!!i!!j 

--Set a delta value.
setDelta :: AFD -> String -> String -> Maybe String -> AFD
setDelta (AFD alp st f i d) s c to = AFD alp st f i (setDeltaD' d (belongs st s) (belongs alp c) to)
		
--Set, if possible, the edge from the delta matrix.
setDeltaD' :: DELTASI -> Int -> Int -> Maybe String-> DELTASI 
setDeltaD' d (-1) _ _ = d
setDeltaD' d _ (-1) _ = d
setDeltaD' (xs:xss) i j to 
	|i == 0 = (setD' xs to j):xss
	|otherwise = xs:(setDeltaD' xss (i-1) j to)

setD' :: [Maybe String] -> Maybe String -> Int -> [Maybe String]
setD' [] _ _ = []
setD' (x:xs) to 0 = to:xs
setD' (x:xs) to n = x:(setD' xs to (n-1))

--Adds a trap state into an AFD.
addTrap :: AFD -> AFD
addTrap (AFD alp st fs is d) = AFD alp ("TT":st) fs is (addTrap' (d++[(createList (length alp) (Just "TT"))]))

addTrap' :: DELTASI -> DELTASI
addTrap' [] = []
addTrap' (xs:xss) = (addTrap'' xs):(addTrap' xss)

addTrap'' :: [Maybe String] -> [Maybe String]
addTrap'' [] = []
addTrap'' (Nothing:xs) = (Just "TT"):(addTrap'' xs)

		------------------
		--TRANSFORMATION--
		------------------

--Converts an AFD into a regular grammar.
toGR :: AFD -> Maybe GR
toGR (AFD a st fs is d)
	|belongs fs is == -1 = createGR st a (makePredicates d st fs a) is 
	|otherwise = createGR st a (addMap is "\\" (makePredicates d st fs a)) is   

--Adds a predicate into a map.
addMap :: String -> String -> HM.HashMap String [String] -> HM.HashMap String [String]
addMap k val m = HM.insert k (val:(getHM (HM.lookup k m))) m

--Converts a delta matrix into predicates.
makePredicates :: DELTASI -> STATES -> STATES -> ALPHABET -> HM.HashMap String [String]
makePredicates [] _ _ _ = HM.empty
makePredicates (xs:xss) (st:sts) fs a = HM.insert st (productions xs fs a) (makePredicates xss sts fs a) 

--Resolves a list of productions into predicates.
productions :: [Maybe String] -> STATES -> ALPHABET -> [String]
productions [] _ _ = []
productions (p:ps) fs (a:as) 
	|evalProduction p fs a == [] = (productions ps fs as)
	|otherwise = (evalProduction p fs a)++(productions ps fs as) 

--Converts a delta into a list of valid productions.
evalProduction :: Maybe String  -> STATES -> String -> [String]
evalProduction Nothing _ _ = []
evalProduction (Just s) fs a 
	|belongs fs s == -1 = (a++" "++s):[]
	|otherwise = a:(a++" "++s):[]

--Converts an AFD into an AFND.
afdToAFND :: AFD -> AFND
afdToAFND (AFD a st fs is d) = (AFND a st fs is (diToDL d))

diToDL :: DELTASI -> DELTASL
diToDL [] = []
diToDL (xs:xss) = (map (evalToList) xs):(diToDL xss) 

--Converts to AFNDL
afdToAFNDL :: AFD -> AFNDL 
afdToAFNDL a = afndToAFNDL (afdToAFND a)


	-------------------------------------
	--NON DETERMINISTIC FINITE AUTOMATA--
	-------------------------------------

		------------
		--CREATION--
		------------

--Creates, if possible, an AFND.
createAFND :: ALPHABET -> STATES -> STATES -> String -> Maybe AFND
createAFND a s f i 
	|belongsAll s (i:f) = Just (AFND a s f i (createList (length s) (createList (length a) [])))
	|otherwise = Nothing

		--------------
		--VALIDATION--
		--------------

--Checks if an AFND is valid.
isAFND :: Maybe AFND -> Bool
isAFND (Just a) = True
isAFND _ = False

		----------------
		--MODIFICATION--
		----------------

--Returns, if possible, the edges from a state with an alphabet character.
getDeltaND :: AFND -> String -> String -> [String]
getDeltaND (AFND alp st _ _ d) s c = auxDelta (belongs st s) (belongs alp c) d

--Set a delta value.
setDeltaND :: AFND -> String -> String -> [String] -> AFND
setDeltaND (AFND alp st f i d) s c to = AFND alp st f i (setDelta' d (belongs st s) (belongs alp c) to)

--Gets, if possible, the edge from the delta matrix.
auxDelta :: Int -> Int -> DELTASL -> [String]
auxDelta (-1) _ _ = []
auxDelta _ (-1) _ = []
auxDelta i j d = d!!i!!j 

		------------------
		--TRANSFORMATION--
		------------8------

--TODO

		--Converts an AFND into an AFD.
toAFD :: AFND -> AFD
toAFD  (AFND alp sts fsts is d) = toAFDAux (AFND alp sts fsts is d) ((AFD alp [] (isFinal fsts is) is []), [[is]]) 

toAFDAux :: AFND -> (AFD, [STATES]) -> AFD 
toAFDAux _ (a ,[]) = a
toAFDAux afnd (af, (p:ps)) = toAFDAux afnd (toAFDAux' afnd (af, ps) p (concatSts p)) 

toAFDAux' :: AFND -> (AFD, [STATES])-> STATES -> String -> (AFD, [STATES])
toAFDAux' (AFND [] _ _ _ _ ) af _ _ = af
toAFDAux' (AFND (a:as) sts fsts is d) ((AFD alp nst nfs nis nd), ps) p cname = toAFDAux' (AFND as sts fsts is d) ((toAFDIT (AFND alp sts fsts is d) ((lastAUX (AFD alp nst nfs nis nd) cname), ps) cname (concatDelta (AFND alp sts fsts is d) p a)) a) p cname

lastAUX :: AFD -> String -> AFD
lastAUX (AFD alp nst nfs nis nd) cname 
	|belongs nst cname == -1 = (AFD alp (cname:nst) nfs nis ((createList (length alp) Nothing):nd))
	|otherwise = (AFD alp nst nfs nis nd)

toAFDIT :: AFND -> (AFD, [STATES]) -> String -> STATES -> String-> (AFD, [STATES])
toAFDIT _ a _ [] _ = a
toAFDIT afnd (af, ps) cname delta char = toAFDIT' afnd (af, ps) cname delta (concatSts delta) char 

toAFDIT' :: AFND -> (AFD, [STATES]) -> String -> STATES -> String -> String -> (AFD, [STATES])
toAFDIT' _ a _ [] _ _ = a
toAFDIT' (AFND alpis sts fsts is d) ((AFD alp nst nfs nis nd), ps) cname delta cdelta char = lastCase ((setDelta (AFD alp nst nfs nis nd) cname char (Just cdelta)), ps) delta cdelta fsts

lastCase :: (AFD, [STATES]) -> STATES -> String -> STATES -> (AFD, [STATES])
lastCase ((AFD alp st fs is d), ps) delta cdelta finales
	|and [(belongs ps delta == -1), (belongs st cdelta == -1)] = ((AFD alp st (updatefs fs delta cdelta finales) is d), (ps++(delta:[])))  
	|otherwise = ((AFD alp st fs is d), ps)

updatefs :: STATES -> STATES -> String -> STATES -> STATES 
updatefs [] a _ _= a
updatefs a [] _ _= a
updatefs st ds cdelta finales
	|disjoint finales ds = st
	|otherwise = cdelta:st

isFinal :: STATES -> String -> STATES
isFinal [] _ = []
isFinal x s 
	|belongs x s == -1 = []
	|otherwise = s:[]


concatSts :: STATES -> String
concatSts sts = concatSts' (IS.sort sts)

concatSts' :: STATES -> String
concatSts' [] = ""
concatSts' (st:sts) = st++(concatSts' sts) 

concatDelta :: AFND -> STATES -> String -> STATES
concatDelta _ [] _ = []
concatDelta a (st:sts) c = IS.union (getDeltaND a st c) (concatDelta a sts c)

--Converts an AFND into an AFNDL.
afndToAFNDL :: AFND -> AFNDL
afndToAFNDL (AFND a b c d e) = AFNDL a b c d (map (++[[]]) e) 

--Converts an AFND into a regular grammar.
dToGR :: AFND -> Maybe GR
dToGR a = toGR (toAFD a)
 
-------------------------------------------
--NON DETERMINISTIC LAMDA FINITE AUTOMATA--
-------------------------------------------

		------------
		--CREATION--
		------------

--Creates, if possible, an AFNDL.
createAFNDL :: ALPHABET -> STATES -> STATES -> String -> Maybe AFNDL
createAFNDL a s f i 
	|belongsAll s (i:f) = Just (AFNDL a s f i (createList (length s) (createList (length a + 1) [])))
	|otherwise = Nothing

		--------------
		--VALIDATION--
		--------------

--Checks if an AFNDL is valid.
isAFNDL :: Maybe AFNDL -> Bool
isAFNDL (Just a) = True
isAFNDL _ = False

		----------------
		--MODIFICATION--
		----------------

--Creates a list.
createList :: Int -> a -> [a]
createList 0 _ = []
createList n a = a:(createList (n-1) a)

--Returns, if possible, the edges from a state with an alphabet character.
getDeltaNDL :: AFNDL -> String -> String -> [String]
getDeltaNDL (AFNDL alp st _ _ d) s "\\" = last (get' d (belongs st s))
getDeltaNDL (AFNDL alp st _ _ d) s c = auxDelta (belongs st s) (belongs alp c) d 

get' :: DELTASL -> Int -> [[String]]
get' d (-1) = []
get' d i = d!!i

--Set a delta value.
setDeltaNDL :: AFNDL -> String -> String -> [String] -> AFNDL
setDeltaNDL (AFNDL alp st f i d) s "\\" to = AFNDL alp st f i (setDelta' d (belongs st s) (length alp) to)
setDeltaNDL (AFNDL alp st f i d) s c to = AFNDL alp st f i (setDelta' d (belongs st s) (belongs alp c) to)
		
--Set, if possible, the edge from the delta matrix.
setDelta' :: DELTASL -> Int -> Int -> [String]-> DELTASL 
setDelta' d (-1) _ _ = d
setDelta' d _ (-1) _ = d
setDelta' (xs:xss) i j to 
	|i == 0 = (set' xs to j):xss
	|otherwise = xs:(setDelta' xss (i-1) j to)

set' :: [[String]] -> [String] -> Int -> [[String]]
set' [] _ _ = []
set' (x:xs) to j
	|j == 0 = to:xs
	|otherwise = x:(set' xs to (j-1))

		------------------
		--TRANSFORMATION--
		------------------

--Converts an AFNDL into a regular grammar.
dlToGR :: AFNDL -> Maybe GR
dlToGR a = toGR (toAFD' a)

--Converts an AFNDL into an AFD.
toAFD' :: AFNDL -> AFD
toAFD' a = toAFD (toAFND a)

--Converts an AFNDL into an AFND.
toAFND :: AFNDL -> AFND
toAFND (AFNDL alp sts f i d) = populate (AFNDL alp sts f i d) (create' (createAFND alp sts (finalStateList (AFNDL alp sts f i d) sts) i)) sts 

populate :: AFNDL -> AFND -> STATES -> AFND
populate _ a [] = a
populate af (AFND alp sts f i d) (x:xs) = populate af (populate' af (AFND alp sts f i d) x alp) xs

populate' :: AFNDL -> AFND -> String -> ALPHABET -> AFND
populate' _ afnd _ [] = afnd
populate' af (AFND alp sts f i d) x (c:cs) = populate' af (setDeltaND (AFND alp sts f i d) x c (magicLamda af x c)) x cs

magicLamda :: AFNDL -> String -> String -> STATES
magicLamda af s c = lamdaClosure af (magicDelta af (lamdaClosure af (s:[])) c) 

magicDelta :: AFNDL -> [String] -> String -> [String]
magicDelta _ [] _ = []
magicDelta af (st:sts) c = checkDelta (getDeltaNDL af st c) (magicDelta af sts c)

checkDelta :: [String] -> [String] -> [String]
checkDelta [] l = l 
checkDelta l [] = l 
checkDelta l (x:xs) 
	|belongs l x == -1 = checkDelta (x:l) xs 
	|otherwise = checkDelta l xs

create' :: Maybe AFND -> AFND 
create' (Just a) = a

--Gets a list of final states.
finalStateList :: AFNDL -> STATES -> STATES
finalStateList _ [] = []
finalStateList (AFNDL alp sts f i d) (x:xs) 
	|disjoint (lamdaClosure (AFNDL alp sts f i d) (x:[])) f = finalStateList (AFNDL alp sts f i d) xs 
	|otherwise = x:(finalStateList (AFNDL alp sts f i d) xs) 

--Gets the lamda closure of a state.
lamdaClosure :: AFNDL -> [String] -> [String]
lamdaClosure (AFNDL alp sts f i d) st 
	|not (belongsAll sts st) = []
	|otherwise = checkSize ((lamdaClosure' (AFNDL alp sts f i d) st st), st) (AFNDL alp sts f i d)

lamdaClosure' :: AFNDL -> [String] -> [String]-> [String]
lamdaClosure' _ new [] = new 
lamdaClosure' af new (x:xs) = lamdaClosure' af ((removeAll (getDeltaNDL af x "\\") new)++new) xs

checkSize :: ([String], [String]) -> AFNDL -> [String] 
checkSize (new, old) af
	|length new == length old = new
	|otherwise = checkSize ((lamdaClosure' af new new), new) af